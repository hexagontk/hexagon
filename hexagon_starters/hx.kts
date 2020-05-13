#!kotlinc -script

// Sources: gh repos, zip files, directories
// Shortcuts (transform into URLs): ghuser/repo, mvnCoordinates, directory
// Metadata inside a file as commands:
// :ask param "Description", "Default value"
// :mv file.ext newFile.ext2
// :replace **/*.kt "src", "dest"
// :check file
// :append file "Text"
val bytes = java.net.URL("https://github.com/hexagonkt/hexagon/archive/master.zip").readBytes()
java.io.File("build/master.zip").writeBytes(bytes)

println("Hello")

