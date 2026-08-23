package eisenbahngesellschaft.strategie;

import eisenbahngesellschaft.model.Bahnhof;
import eisenbahngesellschaft.model.Strecke;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Parallele Variante von {@link BeidseitigesWarten}.
 *
 * <p>
 * Die fachliche Logik bleibt identisch, lediglich die Variantenberechnung fuer
 * die 60 moeglichen Rueckfahrt-Verschiebungen wird parallel ausgefuehrt.
 * </p>
 */
public class BeidSeitigesWartenParallel implements FahrplanStrategie {

	@Override
	public List<Strecke> ermittleFahrplan(List<Strecke> strecken) {
		if (strecken == null || strecken.isEmpty()) return new ArrayList<>();

		int startzeitHinfahrt = strecken.get(0).getBahnhof1().getHinAbfahrt();

		// --- Einfache Fahrt ---
		List<Strecke> einfacheFahrtPlan = kopiereFahrplan(strecken);
		berechneZeiten(einfacheFahrtPlan, -1, startzeitHinfahrt, new int[strecken.size()], new int[strecken.size()]);
		if (!enthaeltKollisionen(einfacheFahrtPlan)) {
			return einfacheFahrtPlan;
		}

		// --- Rueckfahrt verschieben (parallel), Auswahl bleibt deterministisch von 0..59 ---
		List<List<Strecke>> verschobenePlaene = berechneRueckfahrtVerschiebungParallel(strecken, startzeitHinfahrt);
		for (List<Strecke> plan : verschobenePlaene) {
			if (!enthaeltKollisionen(plan)) {
				return plan;
			}
		}

		// Baseline: Einseitiges Warten als Mindestloesung
		FahrplanStrategie einseitig = new EinseitigesWarten();
		List<Strecke> baselinePlan = einseitig.ermittleFahrplan(kopiereFahrplan(strecken));

		int niedrigsteStrafpunkte = berechneScoreAusFahrplan(baselinePlan);
		List<Strecke> besterFahrplan = baselinePlan;

		// --- Greedy-Varianten 0..59 parallel rechnen, Auswertung in fester Reihenfolge ---
		List<GreedyErgebnis> ergebnisse = berechneGreedyVariantenParallel(strecken, startzeitHinfahrt);
		for (GreedyErgebnis ergebnis : ergebnisse) {
			if (ergebnis.fahrplanStabil && ergebnis.strafpunkte < niedrigsteStrafpunkte) {
				niedrigsteStrafpunkte = ergebnis.strafpunkte;
				besterFahrplan = kopiereFahrplan(ergebnis.fahrplan);
			}
		}

		return besterFahrplan;
	}

	private List<List<Strecke>> berechneRueckfahrtVerschiebungParallel(List<Strecke> strecken, int startzeitHinfahrt) {
		List<Callable<List<Strecke>>> aufgaben = new ArrayList<>();
		for (int verschiebung = 0; verschiebung < 60; verschiebung++) {
			final int aktuelleVerschiebung = verschiebung;
			aufgaben.add(() -> {
				List<Strecke> verschobenerPlan = kopiereFahrplan(strecken);
				berechneZeiten(verschobenerPlan, aktuelleVerschiebung, startzeitHinfahrt, new int[strecken.size()], new int[strecken.size()]);
				return verschobenerPlan;
			});
		}
		return fuehreParallelAus(aufgaben);
	}

	private List<GreedyErgebnis> berechneGreedyVariantenParallel(List<Strecke> strecken, int startzeitHinfahrt) {
		List<Callable<GreedyErgebnis>> aufgaben = new ArrayList<>();

		for (int verschiebung = 0; verschiebung < 60; verschiebung++) {
			final int aktuelleVerschiebung = verschiebung;
			aufgaben.add(() -> berechneGreedyVariante(strecken, startzeitHinfahrt, aktuelleVerschiebung));
		}

		return fuehreParallelAus(aufgaben);
	}

	private GreedyErgebnis berechneGreedyVariante(List<Strecke> strecken, int startzeitHinfahrt, int verschiebung) {
		int[] wartezeitenHin = new int[strecken.size()];
		int[] wartezeitenRueck = new int[strecken.size()];

		List<Strecke> aktuellerFahrplan = kopiereFahrplan(strecken);
		boolean fahrplanStabil = false;
		int durchlaeufe = 0;

		while (!fahrplanStabil && durchlaeufe < 100000) {
			berechneZeiten(aktuellerFahrplan, verschiebung, startzeitHinfahrt, wartezeitenHin, wartezeitenRueck);

			int kollisionsIndex = -1;
			for (int i = 0; i < aktuellerFahrplan.size(); i++) {
				if (aktuellerFahrplan.get(i).isKollision()) {
					kollisionsIndex = i;
					break;
				}
			}

			if (kollisionsIndex == -1) {
				fahrplanStabil = true;
			} else {
				Strecke streckeMitKollision = aktuellerFahrplan.get(kollisionsIndex);

				int abfahrtHin = streckeMitKollision.getBahnhof1().getHinAbfahrt();
				int ankunftRueck = streckeMitKollision.getBahnhof1().getRueckAnkunft();
				int zusaetzlicheWartezeitHin = (ankunftRueck + Strecke.EINSTIEGSZEIT - abfahrtHin) % 60;
				if (zusaetzlicheWartezeitHin < 0) zusaetzlicheWartezeitHin += 60;

				int abfahrtRueck = streckeMitKollision.getBahnhof2().getRueckAbfahrt();
				int ankunftHin = streckeMitKollision.getBahnhof2().getHinAnkunft();
				int zusaetzlicheWartezeitRueck = (ankunftHin + Strecke.EINSTIEGSZEIT - abfahrtRueck) % 60;
				if (zusaetzlicheWartezeitRueck < 0) zusaetzlicheWartezeitRueck += 60;

				int gesamteWartezeitHin = 0;
				int gesamteWartezeitRueck = 0;
				for (int wartezeit : wartezeitenHin) gesamteWartezeitHin += wartezeit;
				for (int wartezeit : wartezeitenRueck) gesamteWartezeitRueck += wartezeit;

				int strafpunkteLokalHin = (int) Math.pow(wartezeitenHin[kollisionsIndex] + zusaetzlicheWartezeitHin, 2)
						- (int) Math.pow(wartezeitenHin[kollisionsIndex], 2);
				int strafpunkteLokalRueck = (int) Math.pow(wartezeitenRueck[kollisionsIndex] + zusaetzlicheWartezeitRueck, 2)
						- (int) Math.pow(wartezeitenRueck[kollisionsIndex], 2);

				int strafpunkteUngleichgewichtHin = (int) Math.pow(Math.abs((gesamteWartezeitHin + zusaetzlicheWartezeitHin) - gesamteWartezeitRueck), 2);
				int strafpunkteUngleichgewichtRueck = (int) Math.pow(Math.abs(gesamteWartezeitHin - (gesamteWartezeitRueck + zusaetzlicheWartezeitRueck)), 2);

				long strafpunkteGesamtHin = (kollisionsIndex == 0) ? Long.MAX_VALUE : (long) strafpunkteLokalHin + strafpunkteUngleichgewichtHin;
				long strafpunkteGesamtRueck = (kollisionsIndex == aktuellerFahrplan.size() - 1) ? Long.MAX_VALUE : (long) strafpunkteLokalRueck + strafpunkteUngleichgewichtRueck;

				if (strafpunkteGesamtHin <= strafpunkteGesamtRueck && strafpunkteGesamtHin != Long.MAX_VALUE) {
					wartezeitenHin[kollisionsIndex] += zusaetzlicheWartezeitHin;
				} else if (strafpunkteGesamtRueck != Long.MAX_VALUE) {
					wartezeitenRueck[kollisionsIndex] += zusaetzlicheWartezeitRueck;
				} else {
					break;
				}
			}
			durchlaeufe++;
		}

		if (!fahrplanStabil) {
			return new GreedyErgebnis(false, Integer.MAX_VALUE, null);
		}

		int aktuelleStrafpunkte = 0;
		for (int wartezeit : wartezeitenHin) aktuelleStrafpunkte += (int) Math.pow(wartezeit, 2);
		for (int wartezeit : wartezeitenRueck) aktuelleStrafpunkte += (int) Math.pow(wartezeit, 2);

		return new GreedyErgebnis(true, aktuelleStrafpunkte, aktuellerFahrplan);
	}

	private <T> List<T> fuehreParallelAus(List<Callable<T>> aufgaben) {
		int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		try {
			List<Future<T>> futures = executorService.invokeAll(aufgaben);
			List<T> ergebnisse = new ArrayList<>(futures.size());
			for (Future<T> future : futures) {
				ergebnisse.add(future.get());
			}
			return ergebnisse;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Parallelberechnung wurde unterbrochen.", e);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Fehler in paralleler Berechnung.", e.getCause());
		} finally {
			executorService.shutdown();
		}
	}

	private boolean enthaeltKollisionen(List<Strecke> fahrplan) {
		for (Strecke strecke : fahrplan) {
			if (strecke.isKollision()) return true;
		}
		return false;
	}

	private void berechneZeiten(List<Strecke> fahrplan, int verschiebung, int startzeitHinfahrt, int[] wartezeitenHin, int[] wartezeitenRueck) {

		fahrplan.get(0).getBahnhof1().setHinAnkunft(-1);
		fahrplan.get(0).getBahnhof1().setRueckAbfahrt(-1);
		fahrplan.get(fahrplan.size() - 1).getBahnhof1().setRueckAnkunft(-1);

		fahrplan.get(0).getBahnhof1().setHinAbfahrt(startzeitHinfahrt);

		for (int i = 0; i < fahrplan.size(); i++) {
			Strecke aktuelleStrecke = fahrplan.get(i);
			Bahnhof startBahnhof = aktuelleStrecke.getBahnhof1();
			Bahnhof zielBahnhof = aktuelleStrecke.getBahnhof2();

			if (i > 0) {
				startBahnhof.setHinAbfahrt(startBahnhof.getHinAbfahrt() + wartezeitenHin[i]);
			}

			int ankunftszeit = startBahnhof.getHinAbfahrt() + aktuelleStrecke.getDauer();
			zielBahnhof.setHinAnkunft(ankunftszeit);

			int abfahrtszeit = ankunftszeit + Strecke.EINSTIEGSZEIT;
			zielBahnhof.setHinAbfahrt(abfahrtszeit);

			if (i < fahrplan.size() - 1) {
				Bahnhof naechsterStartBahnhof = fahrplan.get(i + 1).getBahnhof1();
				naechsterStartBahnhof.setHinAbfahrt(abfahrtszeit);
				naechsterStartBahnhof.setHinAnkunft(ankunftszeit);
			}
		}

		Bahnhof letzterBahnhof = fahrplan.get(fahrplan.size() - 1).getBahnhof2();

		int fruehesterStartRueckfahrt = letzterBahnhof.getHinAnkunft() + Strecke.EINSTIEGSZEIT;
		int absoluterStartRueckfahrt = fruehesterStartRueckfahrt;

		if (verschiebung != -1) {
			while (absoluterStartRueckfahrt % 60 != verschiebung) {
				absoluterStartRueckfahrt++;
			}
		}

		letzterBahnhof.setRueckAbfahrt(absoluterStartRueckfahrt);

		for (int i = fahrplan.size() - 1; i >= 0; i--) {
			Strecke aktuelleStrecke = fahrplan.get(i);
			Bahnhof startBahnhofRueck = aktuelleStrecke.getBahnhof2();
			Bahnhof zielBahnhofRueck = aktuelleStrecke.getBahnhof1();

			if (i < fahrplan.size() - 1) {
				startBahnhofRueck.setRueckAbfahrt(startBahnhofRueck.getRueckAbfahrt() + wartezeitenRueck[i]);
			}

			int ankunftszeitRueck = startBahnhofRueck.getRueckAbfahrt() + aktuelleStrecke.getDauer();
			zielBahnhofRueck.setRueckAnkunft(ankunftszeitRueck);

			int abfahrtszeitRueck = ankunftszeitRueck + Strecke.EINSTIEGSZEIT;
			zielBahnhofRueck.setRueckAbfahrt(abfahrtszeitRueck);

			if (i > 0) {
				Bahnhof vorherigerZielBahnhof = fahrplan.get(i - 1).getBahnhof2();
				vorherigerZielBahnhof.setRueckAbfahrt(abfahrtszeitRueck);
				vorherigerZielBahnhof.setRueckAnkunft(ankunftszeitRueck);
			}

			aktuelleStrecke.pruefeKollision();
		}
	}

	private int berechneScoreAusFahrplan(List<Strecke> fahrplan) {
		int score = 0;

		for (int i = 1; i < fahrplan.size(); i++) {
			Bahnhof bhf = fahrplan.get(i).getBahnhof1();
			int ankunftVorher = fahrplan.get(i - 1).getBahnhof2().getHinAnkunft();
			int fruehesteAbfahrt = ankunftVorher + Strecke.EINSTIEGSZEIT;

			int wartezeit = (bhf.getHinAbfahrt() - fruehesteAbfahrt) % 60;
			if (wartezeit < 0) wartezeit += 60;

			score += (wartezeit * wartezeit);
		}

		for (int i = fahrplan.size() - 2; i >= 0; i--) {
			Bahnhof bhf = fahrplan.get(i).getBahnhof2();
			int ankunftVorher = fahrplan.get(i + 1).getBahnhof1().getRueckAnkunft();
			int fruehesteAbfahrt = ankunftVorher + Strecke.EINSTIEGSZEIT;

			int wartezeit = (bhf.getRueckAbfahrt() - fruehesteAbfahrt) % 60;
			if (wartezeit < 0) wartezeit += 60;

			score += (wartezeit * wartezeit);
		}

		return score;
	}

	private List<Strecke> kopiereFahrplan(List<Strecke> original) {
		List<Strecke> kopie = new ArrayList<>();
		for (Strecke strecke : original) {
			kopie.add(strecke.copy());
		}
		return kopie;
	}

	@Override
	public String getName() {
		return "Beidseitiges Warten Parallel";
	}

	private static final class GreedyErgebnis {
		private final boolean fahrplanStabil;
		private final int strafpunkte;
		private final List<Strecke> fahrplan;

		private GreedyErgebnis(boolean fahrplanStabil, int strafpunkte, List<Strecke> fahrplan) {
			this.fahrplanStabil = fahrplanStabil;
			this.strafpunkte = strafpunkte;
			this.fahrplan = fahrplan;
		}
	}
}
