package com.ossanasur.cbconnect.exception;

import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<DataResponse<Void>> handleNotFound(RessourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new DataResponse<>(new Date(), false, "Ressource inexistante : " + ex.getMessage(), 404, null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DataResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DataResponse<>(new Date(), false, "Erreur de demande : " + ex.getMessage(), 400, null));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<DataResponse<Void>> handleAlreadyExist(AlreadyExistException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new DataResponse<>(new Date(), false, ex.getMessage(), 409, null));
    }

    @ExceptionHandler(NoChangesDetectedException.class)
    public ResponseEntity<DataResponse<Void>> handleNoChanges(NoChangesDetectedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(
                DataResponse.<Void>builder().isSuccess(true).message(ex.getMessage()).build());
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<DataResponse<Void>> handleNotAuthorized(NotAuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new DataResponse<>(new Date(), false, "Action non autorisee : " + ex.getMessage(), 403, null));
    }

    @ExceptionHandler(LockedAccountException.class)
    public ResponseEntity<DataResponse<Void>> handleLockedAccount(LockedAccountException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(
                new DataResponse<>(new Date(), false, "Compte bloque : " + ex.getMessage(), 406, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new DataResponse<>(new Date(), false, "Validation echouee : " + errors, 400, null));
    }

    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public ResponseEntity<DataResponse<Void>> handleSql(Exception ex) {
        log.error("Erreur SQL : ", ex);
        String raw = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new DataResponse<>(new Date(), false, formaterMessageSql(raw), 500, null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DataResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("RuntimeException : ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new DataResponse<>(new Date(), false, ex.getMessage(), 500, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse<Void>> handleGeneric(Exception ex) {
        log.error("Exception generique : ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new DataResponse<>(new Date(), false, "Erreur interne : " + ex.getMessage(), 500, null));
    }

    private String formaterMessageSql(String message) {
        if (message == null || message.isBlank()) return "Erreur base de donnees.";
        String msg = message.toLowerCase(Locale.ROOT);
        if (msg.contains("duplicate key") || msg.contains("cle dupliquee")) {
            String champ = extraireChamp(message);
            return champ != null ? "La valeur du champ \"" + champ + "\" existe deja." : "Une donnee unique existe deja.";
        }
        if (msg.contains("foreign key constraint")) return "Cette donnee est utilisee ailleurs et ne peut pas etre supprimee.";
        if (msg.contains("null value in column")) {
            String champ = extraireChamp(message);
            return champ != null ? "Le champ \"" + champ + "\" est obligatoire." : "Un champ obligatoire est manquant.";
        }
        if (msg.contains("timeout") || msg.contains("deadlock")) return "La base de donnees est temporairement indisponible.";
        return "Erreur de base de donnees : " + message;
    }

    private String extraireChamp(String message) {
        int start = message.indexOf('('), end = message.indexOf(')');
        if (start >= 0 && end > start) {
            String champ = message.substring(start + 1, end);
            return champ.contains(",") ? champ.split(",")[0].trim() : champ.trim();
        }
        return null;
    }
}
