package javaminnder.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javaminnder.model.Subscription;
import javaminnder.model.User;

/** Gera um arquivo CSV com as assinaturas do usuário, para uso em planilhas. */
public class ExportService {

    private static final String HEADER =
            "id;servico;preco;ciclo;status;proxima_cobranca;equivalente_mensal;equivalente_anual\n";

    private final SubscriptionService subscriptions;
    private final Path exportDirectory;

    public ExportService(SubscriptionService subscriptions, Path exportDirectory) {
        this.subscriptions = subscriptions;
        this.exportDirectory = exportDirectory;
    }

    public Path exportCsv(User user) {
        List<Subscription> items = subscriptions.listFor(user);
        StringBuilder csv = new StringBuilder(HEADER);
        for (Subscription subscription : items) {
            csv.append(subscription.getId()).append(';')
                    .append(escape(subscription.getName())).append(';')
                    .append(subscription.getPrice().toPlainString()).append(';')
                    .append(subscription.getCycle().name()).append(';')
                    .append(subscription.getStatus().name()).append(';')
                    .append(subscription.getNextBillingDate()).append(';')
                    .append(subscription.monthlyEquivalent().toPlainString()).append(';')
                    .append(subscription.yearlyEquivalent().toPlainString()).append('\n');
        }
        try {
            Files.createDirectories(exportDirectory);
            Path target = exportDirectory.resolve(fileName(user));
            Files.writeString(target, csv.toString(), StandardCharsets.UTF_8);
            return target;
        } catch (IOException exception) {
            throw new UncheckedIOException("Não foi possível escrever o CSV.", exception);
        }
    }

    private String fileName(User user) {
        String slug = user.getEmail()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-");
        return "javaminnder-" + slug + "-" + LocalDate.now() + ".csv";
    }

    private String escape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(";") || safe.contains("\"") || safe.contains("\n")) {
            return '"' + safe.replace("\"", "\"\"") + '"';
        }
        return safe;
    }
}
