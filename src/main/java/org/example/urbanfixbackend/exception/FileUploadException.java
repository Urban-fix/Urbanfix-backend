package org.example.urbanfixbackend.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String filename, String reason) {
        super("Error al subir el archivo '" + filename + "': " + reason);
    }
}
