package tech.ydb.apps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tech.ydb.apps.entity.Account;
import tech.ydb.apps.entity.AccountConfig;
import tech.ydb.apps.entity.Saldo;
import tech.ydb.apps.entity.SaldoConfig;
import tech.ydb.apps.entity.SaldoKey;
import tech.ydb.apps.model.PaymentRequest;
import tech.ydb.apps.model.PaymentTask;
import tech.ydb.apps.repository.AccountConfigRepo;
import tech.ydb.apps.repository.AccountRepo;
import tech.ydb.apps.repository.SaldoConfigRepo;
import tech.ydb.apps.repository.SaldoRepo;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Service
public class ConfigService {
    private final static Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private final AccountConfigRepo accountConfigRepo;
    private final SaldoConfigRepo saldoConfigRepo;
    private final AccountRepo accountRepo;
    private final SaldoRepo saldoRepo;

    private final Map<SaldoKey, String> processors = new HashMap<>();
    private final List<Rate> accountRates = new ArrayList<>();
    private final Map<String, List<SaldoKey>> bics = new HashMap<>();

    private int totalWeight = 0;

    public ConfigService(AccountConfigRepo acr, SaldoConfigRepo scr, AccountRepo ar, SaldoRepo sr) {
        this.accountConfigRepo = acr;
        this.saldoConfigRepo = scr;
        this.accountRepo = ar;
        this.saldoRepo = sr;
    }

    public void init() {
        processors.clear();
        accountRates.clear();
        totalWeight = 0;

        for (SaldoConfig config: saldoConfigRepo.findAll()) {
            processors.put(config.getId(), config.getProcessor());
        }

        Map<Long, Integer> weights = new HashMap<>();
        for (AccountConfig config: accountConfigRepo.findAll()) {
            if (config.getWeight() > 0) {
                weights.put(config.getAccountId(), config.getWeight());
            }
        }
        logger.info("loaded {} processors", processors.values().size());

        Map<Long, String> accountBics = new HashMap<>();
        for (Account account: accountRepo.findAll()) {
            int weight = weights.getOrDefault(account.getAccountId(), 1);
            accountRates.add(new Rate(account.getBic(), weight));
            accountBics.put(account.getAccountId(), account.getBic());
            totalWeight += weight;
        }

        logger.info("loaded {} accounts with total weights {}", accountRates.size(), totalWeight);

        long subaccounts = 0;
        for (Saldo saldo: saldoRepo.findAll()) {
            long accId = saldo.getId().getAccountId();
            if (!accountBics.containsKey(accId)) {
                logger.warn("cannot find account with id {}", accId);
                continue;
            }
            String bic = accountBics.get(accId);
            if (!bics.containsKey(bic)) {
                bics.put(bic, new ArrayList<>());
            }
            bics.get(bic).add(saldo.getId());
            subaccounts++;
        }

        logger.info("loaded {} bics with {} saldos", bics.keySet().size(), subaccounts);
    }

    public Collection<String> listProcessors() {
        return processors.values();
    }

    public String findProcessor(SaldoKey saldoKey) {
        return processors.get(saldoKey);
    }

    private List<SaldoKey> listSaldos(String bic) {
        if (!bics.containsKey(bic)) {
            throw new IllegalArgumentException("Unknown bic " + bic);
        }
        return bics.get(bic);
    }

    public PaymentTask createPaymentTask(PaymentRequest req) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<SaldoKey> a = listSaldos(req.getBicA());
        List<SaldoKey> b = listSaldos(req.getBicB());
        // Use random saldos
        SaldoKey keyA = a.get(rnd.nextInt(a.size()));
        SaldoKey keyB = b.get(rnd.nextInt(b.size()));
        return new PaymentTask(UUID.randomUUID().toString(), keyA, keyB, req.getAmount());
    }

    public PaymentRequest createRandomPayment() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        BigDecimal amount = BigDecimal.valueOf(rnd.nextInt(1000000), 2);

        int p1 = rnd.nextInt(totalWeight);
        Rate a1 = null;

        for (Rate rate: accountRates) {
            if (p1 < rate.weight) {
                a1 = rate;
                break;
            }
            p1 -= rate.weight;
        }

        if (a1 != null) {
            int p2 = rnd.nextInt(totalWeight - a1.weight);
            for (Rate rate: accountRates) {
                if (rate == a1) {
                    continue;
                }

                if (p2 < rate.weight) {
                    if (rnd.nextBoolean()) {
                        return new PaymentRequest(a1.bic, rate.bic, amount);
                    } else {
                        return new PaymentRequest(rate.bic, a1.bic, amount);
                    }
                }

                p2 -= rate.weight;
            }
        }

        // fallback, it should never happen
        Rate fa1 = accountRates.get(rnd.nextInt(accountRates.size()));
        Rate fa2 = fa1;
        while (fa2 == fa1) {
            fa2 = accountRates.get(rnd.nextInt(accountRates.size()));
        }
        return new PaymentRequest(fa1.bic, fa2.bic, amount);
    }

    private static class Rate {
        private final String bic;
        private final int weight;

        public Rate(String bic, int weight) {
            this.bic = bic;
            this.weight = weight;
        }
    }
}
