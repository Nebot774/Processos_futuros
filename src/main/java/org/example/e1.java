package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class e1 {

    public static void main(String[] args) {
        // Creación de un objeto Scanner para leer la entrada del usuario
        Scanner escaner = new Scanner(System.in);
        System.out.println("Ingrese una URL:");
        String url = escaner.nextLine();
        escaner.close();

        // Creamos un CompletableFuture para descargar la página web de manera asincrónica
        CompletableFuture<Void> futuro = CompletableFuture.supplyAsync(() -> descargarPaginaWeb(url))
                .thenAccept(System.out::println); // Una vez descargada, imprime el contenido en la consola

        try {
            futuro.get(); // Bloquea y espera hasta que se complete la descarga de la página web esto lo hacemos para
            // que el programa no termine antes de que se descargue la página web
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Método para descargar el contenido de la página web
    private static String descargarPaginaWeb(String url) {
        HttpClient cliente = HttpClient.newHttpClient(); // Creación de un cliente HTTP
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url)) // Configura la URI de la solicitud con la URL ingresada
                .build();

        try {
            // Envía la solicitud y obtiene la respuesta
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            return respuesta.body(); // Devuelve el cuerpo de la respuesta (contenido de la página web)
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(); // Manejo de posibles errores en la descarga
            return "Error al descargar la página web."; // Mensaje de error en caso de falla
        }
    }
}
