package com.bank.helpers;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Sends templated emails via SMTP.
 * Placeholders in templates look like {{name}} and are filled from a Map.
 * Delivery runs on a background thread so the JavaFX UI stays responsive.
 */
public final class MailService {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "mail-sender");
        t.setDaemon(true);
        return t;
    });

    private final Properties config = new Properties();
    private final Session session;

    public MailService() {
        try (InputStream in = getClass().getResourceAsStream("/mail.properties")) {
            if (in == null) {
                throw new IllegalStateException("mail.properties not found on classpath (src/main/resources/mail.properties)");
            }
            config.load(in);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load mail.properties", e);
        }

        Properties smtp = new Properties();
        smtp.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        smtp.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        smtp.put("mail.smtp.auth", config.getProperty("mail.smtp.auth", "true"));
        smtp.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable", "true"));

        final String user = config.getProperty("mail.username");
        final String pass = config.getProperty("mail.password");

        session = Session.getInstance(smtp, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    }

    /** Replace every {{key}} in the template with the matching value from vars. */
    public static String applyTemplate(String template, Map<String, String> vars) {
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            String value = e.getValue() != null ? e.getValue() : "";
            result = result.replace("{{" + e.getKey() + "}}", value);
        }
        return result;
    }

    /**
     * Send HTML email in the background.
     * Callbacks (if non-null) are invoked on the JavaFX application thread.
     */
    public void sendAsync(String to, String subject, String bodyHtml,
                          Runnable onSuccess, Consumer<String> onError) {
        EXECUTOR.submit(() -> {
            try {
                send(to, subject, bodyHtml);
                if (onSuccess != null) {
                    javafx.application.Platform.runLater(onSuccess);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (onError != null) {
                    String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                    javafx.application.Platform.runLater(() -> onError.accept(msg));
                }
            }
        });
    }

    private void send(String to, String subject, String bodyHtml) throws Exception {
        MimeMessage message = new MimeMessage(session);
        String from = config.getProperty("mail.from", config.getProperty("mail.username"));
        String fromName = config.getProperty("mail.from.name", "Bank");
        message.setFrom(new InternetAddress(from, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(bodyHtml, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    // ── loan reminder template ───────────────────────────────────────

    public static final String LOAN_REMINDER_SUBJECT =
        "Loan reminder — {{loan_id}}";

    public static final String LOAN_REMINDER_BODY = """
        <html><body style="font-family: Arial, sans-serif; color: #222;">
          <h2>Loan reminder</h2>
          <p>Dear {{client_name}},</p>
          <p>This is a reminder regarding your loan with our bank.</p>
          <table style="border-collapse: collapse; margin: 16px 0;">
            <tr><td style="padding:4px 12px 4px 0;"><b>Loan ID</b></td><td>{{loan_id}}</td></tr>
            <tr><td style="padding:4px 12px 4px 0;"><b>Amount</b></td><td>{{amount}}</td></tr>
            <tr><td style="padding:4px 12px 4px 0;"><b>Date</b></td><td>{{loan_date}}</td></tr>
            <tr><td style="padding:4px 12px 4px 0;"><b>Account</b></td><td>{{account_id}}</td></tr>
          </table>
          <p>Please contact us if you have already settled this loan or need assistance.</p>
          <p style="color:#666; font-size:12px;">— {{bank_name}}</p>
        </body></html>
        """;
}
