package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.zip.*;

public class e2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduzca la carpeta o ficehro a comprimir:");
        String rutaOrigen = scanner.nextLine();
        System.out.println("Introduzca la ruta destino para el archivo ZIP:");
        String rutaDestino = scanner.nextLine();
        scanner.close();

        // Iniciamos la compresión y movimiento de forma asincrónica
        CompletableFuture.supplyAsync(() -> {
            try {
                return comprimirArchivo(rutaOrigen);//devolvemos el archivo comprimido
            } catch (IOException e) {
                throw new RuntimeException("Error al comprimir el archivo", e);
            }
        }).thenAcceptAsync(archivoZip -> {//una vez comprimido lo movemos
            try {
                moverArchivo(archivoZip, Paths.get(rutaDestino));
                System.out.println("Compresión y movimiento completados con éxito.");
            } catch (IOException e) {
                System.err.println("Error al mover el archivo: " + e.getMessage());
            }
        }).join(); // Esperamos a que las tareas asincrónicas se completen
    }

    // Comprime un archivo o carpeta en un archivo ZIP
    private static Path comprimirArchivo(String rutaOrigen) throws IOException {
        Path archivoZip = Files.createTempFile("compresion", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archivoZip))) {
            Path sourcePath = Paths.get(rutaOrigen);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Error al comprimir el archivo: " + e);
                        }
                    });
        }
        return archivoZip;
    }

    // Mueve un archivo a una ruta destino
    private static void moverArchivo(Path origen, Path destino) throws IOException {
        Files.move(origen, destino.resolve(origen.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }
}

