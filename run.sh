# Schleife über alle Dateien im input-Ordner
for file in input/*; do
    # Entfernt '.txt' vom Ende des Strings
    file_without_ext="${file%.txt}"

    echo "Running for $file"
    java -jar jar/eisenbahngesellschaft.jar "$file_without_ext"
    echo "----------------------------------------"
done