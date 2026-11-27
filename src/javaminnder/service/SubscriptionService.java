package javaminnder.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javaminnder.exception.NotFoundException;
import javaminnder.exception.ValidationException;
import javaminnder.model.BillingCycle;
import javaminnder.model.Subscription;
import javaminnder.model.SubscriptionStatus;
import javaminnder.model.User;
import javaminnder.repository.SubscriptionRepository;
import javaminnder.util.Validators;

/** Regras de negócio das assinaturas, sempre restritas ao usuário logado. */
public class SubscriptionService {

    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_NOTES_LENGTH = 200;

    private final /*@ spec_public @*/ SubscriptionRepository repository;

    //@ public invariant repository != null;

    /*@ public normal_behavior
      @   requires repository != null;
      @   ensures this.repository == repository;
      @*/
    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    public Subscription add(
            User user,
            String name,
            BigDecimal price,
            BillingCycle cycle,
            LocalDate nextBillingDate,
            String notes) {
        return add(user, name, price, cycle, SubscriptionStatus.ACTIVE, nextBillingDate, notes);
    }

    public Subscription add(
            User user,
            String name,
            BigDecimal price,
            BillingCycle cycle,
            SubscriptionStatus status,
            LocalDate nextBillingDate,
            String notes) {
        Subscription subscription = new Subscription(
                user.getId(),
                cleanName(name),
                cleanPrice(price),
                requireCycle(cycle),
                status == null ? SubscriptionStatus.ACTIVE : status,
                requireDate(nextBillingDate),
                cleanNotes(notes));
        return repository.save(subscription);
    }

    public Subscription update(
            User user,
            long id,
            String name,
            BigDecimal price,
            BillingCycle cycle,
            LocalDate nextBillingDate,
            SubscriptionStatus status,
            String notes) {
        Subscription subscription = require(user, id);
        subscription.setName(cleanName(name));
        subscription.setPrice(cleanPrice(price));
        subscription.setCycle(requireCycle(cycle));
        subscription.setNextBillingDate(requireDate(nextBillingDate));
        subscription.setStatus(status == null ? SubscriptionStatus.ACTIVE : status);
        subscription.setNotes(cleanNotes(notes));
        subscription.touch();
        return repository.update(subscription);
    }

    public void delete(User user, long id) {
        require(user, id);
        repository.deleteById(id);
    }

    /** Registra o pagamento do ciclo atual e avança a data de cobrança. */
    public Subscription registerPayment(User user, long id) {
        Subscription subscription = require(user, id);
        if (!subscription.isBillable()) {
            throw new ValidationException(
                    "Só é possível registrar pagamento de assinaturas ativas.");
        }
        subscription.renew();
        return repository.update(subscription);
    }

    /*@ public behavior
      @   requires repository != null && user != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public List<Subscription> listFor(User user) {
        /*@ nullable @*/ Long userId = user.getId();
        if (userId == null) {
            throw new IllegalArgumentException("O id do usuário não pode ser nul0.");
        }
        return repository.findByUserId(userId.longValue());
    }
    
    /*@ public behavior
      @   requires repository != null && user != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    /*@ skipesc @*/
    public List<Subscription> listBillableFor(User user) {
        return listFor(user).stream().filter(Subscription::isBillable).collect(Collectors.toList());
    }

    /*@ public behavior
      @   requires repository != null && user != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    /*@ skipesc @*/
    public List<Subscription> search(User user, String query) {
        String term = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Subscription> source = listFor(user);
        if (term.isEmpty()) {
            return source;
        }
        List<Subscription> matches = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            /*@ nullable @*/ Subscription subscription = source.get(i);
            if (subscription == null) {
                continue;
            }
            /*@ nullable @*/ String name = subscription.getName();
            /*@ nullable @*/ String notes = subscription.getNotes();
            boolean nameMatches = name != null && name.toLowerCase(Locale.ROOT).contains(term);
            boolean notesMatches = notes != null && notes.toLowerCase(Locale.ROOT).contains(term);
            if (nameMatches || notesMatches) {
                matches.add(subscription);
            }
        }
        return matches;
    }

    /** Busca uma assinatura garantindo que ela pertence ao usuário informado. */
    /*@ public behavior
      @   requires repository != null && user != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public Subscription require(User user, long id) {
        Subscription subscription = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assinatura não encontrada: id " + id));
        if (subscription.getUserId() == null || !subscription.getUserId().equals(user.getId())) {
            throw new NotFoundException("Assinatura não encontrada: id " + id);
        }
        return subscription;
    }

    /*@ public behavior
      @   requires repository != null;
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    public List<Subscription> findAll() {
        return repository.findAll();
    }

    /*@ private behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    private String cleanName(String name) {
        return Validators.requireText(name, "nome do serviço", 2, MAX_NAME_LENGTH);
    }
    
    /*@ private behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    private BigDecimal cleanPrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("Informe o preço da assinatura.");
        }
        if (price.signum() <= 0) {
            throw new ValidationException("O preço precisa ser maior que zero.");
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /*@ private behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    private BillingCycle requireCycle(BillingCycle cycle) {
        if (cycle == null) {
            throw new ValidationException("Escolha um ciclo de cobrança.");
        }
        return cycle;
    }
    
    /*@ private behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    private LocalDate requireDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Informe a próxima data de cobrança.");
        }
        return date;
    }

    /*@ private behavior
      @   assignable \everything;
      @   ensures \result != null;
      @*/
    private String cleanNotes(String notes) {
        String clean = notes == null ? "" : notes.trim();
        if (clean.length() > MAX_NOTES_LENGTH) {
            throw new ValidationException(
                    "As observações aceitam no máximo " + MAX_NOTES_LENGTH + " caracteres.");
        }
        return clean;
    }
}
