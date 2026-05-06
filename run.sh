


echo "Kompiliere Java-Dateien..."
# Sucht automatisch ALLE .java Dateien im src-Ordner und kompiliert sie in den out-Ordner
javac -d out/production/GroPro-IHK $(find src -name "*.java")

# Überprüfen, ob das Kompilieren erfolgreich war (keine Syntaxfehler im Code)
if [ $? -ne 0 ]; then
    echo "Kompilierungsfehler! Skript wird abgebrochen."
    exit 1
fi

echo "Kompilieren erfolgreich. Starte Testfälle..."
echo "----------------------------------------"

# Schleife über alle Dateien im input-Ordner
for file in input/*; do
    echo "Running for $file"
    # Führe das frisch kompilierte Java-Programm aus
    java -cp out/production/GroPro-IHK eisenbahngesellschaft.Main "$file"
    echo "----------------------------------------"
done