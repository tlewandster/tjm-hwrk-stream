package homework;

import homework.generator.HoldingGenerator;
import homework.model.*;
import homework.model.Currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Exercises {

    private static final List<Holding> holdings = new HoldingGenerator().generate();

    public static void main(String[] args) {
        raport();
    }

    // =================================================================================
    //  CZĘŚĆ A – podstawy (★)
    // =================================================================================

    /**
     * Napisz metodę, która zwróci liczbę holdingów, w których jest przynajmniej jedna firma.
     */
    public static long getHoldingsWhereAreCompanies() {
        return holdings.stream()
                .filter(holding -> !holding.getCompanies().isEmpty())
                .count();
    }

    /**
     * Napisz metodę, która zwróci nazwy wszystkich holdingów pisane z wielkiej litery w formie listy.
     */
    public static List<String> getHoldingNames() {
        return holdings.stream()
                .map(holding -> holding.getName().toUpperCase())
                .toList();
    }

    /**
     * Zwraca nazwy wszystkich holdingów sklejone w jeden string i posortowane.
     * String ma postać: (Coca-Cola, Nestle, Pepsico)
     */
    public static String getHoldingNamesAsString() {
        return holdings.stream()
                .map(Holding::getName)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Zwraca liczbę firm we wszystkich holdingach.
     */
    public static long getCompaniesAmount() {
        return holdings.stream()
                .mapToInt(holding -> holding.getCompanies().size())
                .sum();
    }


    /**
     * Zwraca liczbę wszystkich pracowników we wszystkich firmach.
     */
    public static long getAllUserAmount() {
        return holdings.stream()
                .flatMapToInt(holding -> holding.getCompanies()
                        .stream()
                        .mapToInt(company -> company.getUsers().size()))
                .sum();
    }

    /**
     * Zwraca listę wszystkich firm jako listę, której implementacja to LinkedList. Obiektów nie przepisujemy
     * po zakończeniu działania strumienia.
     */
    public static LinkedList<String> getAllCompaniesNamesAsLinkedList() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies()
                        .stream()
                        .map(Company::getName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Przelicza kwotę na rachunku na złotówki za pomocą kursu określonego w enum Currency.
     */
    public static BigDecimal getAccountAmountInPLN(Account account) {
        return account.getAmount()
                .multiply(new BigDecimal(String.valueOf(account.getCurrency().getRate())))
                .round(new MathContext(10, RoundingMode.HALF_UP));
    }

    /**
     * Zwraca imiona użytkowników w formie zbioru, którzy spełniają podany warunek.
     */
    public static Set<String> getUsersForPredicate(final Predicate<User> userPredicate) {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .filter(userPredicate)
                .map(User::getFirstName)
                .collect(Collectors.toSet());
    }

    /**
     * Dla każdej firmy uruchamia przekazaną metodę.
     */
    public static void executeForEachCompany(Consumer<Company> consumer) {
        holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .forEach(consumer);
    }

    /**
     * Wyszukuje najbogatsza kobietę i zwraca ją. Metoda musi uzwględniać to że rachunki są w różnych walutach.
     */
    //pomoc w rozwiązaniu problemu w zadaniu: https://stackoverflow.com/a/55052733/9360524
    public static Optional<User> getRichestWoman() {

        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .filter(user -> user.getSex().equals(Sex.WOMAN))
                .sorted(Comparator.comparing(Exercises::getUserAmountInPLN, Comparator.reverseOrder()))
                .findFirst();

    }

    private static BigDecimal getUserAmountInPLN(final User user) {
        return user.getAccounts().stream()
                .map(Exercises::getAccountAmountInPLN)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Zwraca nazwy pierwszych N firm. Kolejność nie ma znaczenia.
     */
    private static Set<String> getFirstNCompany(final int n) {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .map(Company::getName)
                .limit(n)
                .collect(Collectors.toSet());
    }

    /**
     * Zwraca mapę firm, gdzie kluczem jest jej nazwa a wartością lista pracowników.
     */
    public static Map<String, List<User>> getUserPerCompany() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .collect(Collectors.toMap(Company::getName, Company::getUsers));
    }

    /**
     * Zwraca pierwszego z brzegu użytkownika dla podanego warunku. W przypadku kiedy nie znajdzie użytkownika, wyrzuca
     * wyjątek IllegalArgumentException.
     */
    public static User getUser(final Predicate<User> predicate) {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no such person"));
    }

    /**
     * Zwraca mapę rachunków, gdzie kluczem jest numer rachunku, a wartością ten rachunek.
     */
    public static Map<String, Account> createAccountsMap() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream())
                .collect(Collectors.toMap(Account::getNumber, account -> account));
    }

    /**
     * Zwraca listę wszystkich imion w postaci Stringa, gdzie imiona oddzielone są spacją i nie zawierają powtórzeń.
     */
    public static String getUserNames() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .map(User::getFirstName)
                .distinct()
                .collect(Collectors.joining(" "));
    }

    /**
     * Metoda wypisuje na ekranie wszystkich użytkowników (imię, nazwisko) posortowanych od z do a.
     * Zosia Psikuta, Zenon Kucowski, Zenek Jawowy ... Alfred Pasibrzuch, Adam Wojcik
     */
    public static void showAllUser() {
        holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .sorted(Comparator.comparing(User::getFirstName))
                .forEach(user -> System.out.println(user.getFirstName() + " " + user.getLastName()));
    }

    /**
     * Zwraca zbiór walut w jakich są rachunki.
     */
    public static Set<Currency> getCurenciesSet() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream())
                .map(Account::getCurrency)
                .collect(Collectors.toSet());
    }

    /**
     * Zwraca strumień wszystkich firm.
     */
    private static Stream<Company> getCompanyStream() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream());
    }

    /**
     * Tworzy strumień użytkowników.
     */
    private static Stream<User> getUserStream() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream());
    }

    /**
     * Tworzy strumień rachunków.
     */
    private static Stream<Account> getAccountStream() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream())
                .flatMap(company -> company.getUsers().stream())
                .flatMap(user -> user.getAccounts().stream());
    }

    // =================================================================================
    //  CZĘŚĆ B – Collectors, grupowanie, statystyki (★★)
    //
    //  Zasady dla całej części B i C:
    //   - żadnych pętli for/while i żadnych list, do których dopisujesz w trakcie działania
    //     strumienia (wyjątki są wyraźnie opisane w treści zadania),
    //   - kwoty przeliczone na PLN zaokrąglamy zawsze na końcu obliczeń:
    //     setScale(2, RoundingMode.HALF_UP),
    //   - do przeliczania walut używaj metody getAccountAmountInPLN z części A.
    // =================================================================================

    /**
     * Zwraca mapę: kraj firmy -> liczba firm w tym kraju.
     * Podpowiedź: groupingBy + counting.
     */
    public static Map<Country, Long> getCompaniesCountPerCountry() {
        return getCompanyStream()
                .collect(Collectors.groupingBy(
                        Company::getCountry,
                        Collectors.counting())
                );
    }

    /**
     * Zwraca mapę dwupoziomową: region -> kraj -> lista nazw firm.
     * Region wyciągasz z kraju firmy (Country.getRegion()).
     * Podpowiedź: groupingBy w groupingBy, a na końcu mapping.
     */
    public static Map<Region, Map<Country, List<String>>> getCompanyNamesPerRegionAndCountry() {
        return getCompanyStream()
                .collect(Collectors.groupingBy(
                        company -> company.getCountry().getRegion(),
                        Collectors.groupingBy(Company::getCountry,
                                Collectors.mapping(Company::getName, Collectors.toList())
                        )
                ));
    }

    /**
     * Zwraca mapę: nazwa holdingu -> suma wszystkich rachunków wszystkich pracowników holdingu, przeliczona na PLN
     * i zaokrąglona do dwóch miejsc po przecinku.
     * Trzeba zejść trzy poziomy w dół: holding -> firma -> pracownik -> rachunek.
     * Podpowiedź: BigDecimal nie ma Collectors.summingBigDecimal – użyj reduce albo Collectors.reducing.
     */
    public static Map<String, BigDecimal> getTotalBalanceInPlnPerHolding() {
        return holdings.stream()
                .collect(Collectors.toMap(
                                Holding::getName,
                                holding -> holding.getCompanies().stream()
                                        .flatMap(company -> company.getUsers().stream())
                                        .flatMap(user -> user.getAccounts().stream())
                                        .map(Exercises::getAccountAmountInPLN)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .setScale(2, RoundingMode.HALF_UP)
                        )
                );
    }

    /**
     * Zwraca mapę: typ rachunku -> lista numerów rachunków tego typu.
     * Mapa ma zachować kolejność zdefiniowaną w enumie AccountType (ROR1, ROR2, RO1, ...).
     * Podpowiedź: groupingBy z trzema argumentami i EnumMap jako fabryką mapy + mapping.
     */
    public static Map<AccountType, List<String>> getAccountNumbersPerType() {
        return getAccountStream()
                .collect(Collectors.groupingBy(Account::getType
                        , () -> new EnumMap<>(AccountType.class)
                        , Collectors.mapping(Account::getNumber, Collectors.toList())
                ));
    }

    /**
     * Dzieli pracowników na dwie grupy: wiek >= podana granica (true) i wiek < granica (false).
     * Wartością w mapie ma być lista napisów w formacie "Imię Nazwisko".
     * Podpowiedź: partitioningBy z kolektorem downstream.
     */
    public static Map<Boolean, List<String>> partitionUserNamesByAge(final int age) {
        return getUserStream()
                .collect(Collectors.partitioningBy(
                        user -> user.getAge() >= age,
                        Collectors.mapping(
                                user -> user.getFirstName() + " " + user.getLastName(),
                                Collectors.toList()))
                );
    }

    /**
     * Zwraca mapę: nazwa firmy -> liczba pracowników, posortowaną malejąco po liczbie pracowników.
     * Przy tej samej liczbie pracowników decyduje alfabetyczna kolejność nazwy firmy.
     * Kolejność MUSI być zachowana po zwróceniu mapy – zwróć uwagę na implementację mapy.
     */
    public static LinkedHashMap<String, Long> getUsersCountPerCompanyDescending() {
        return getCompanyStream()
                .sorted(Comparator.comparingInt((Company company) -> company.getUsers().size()).reversed()
                        .thenComparing(Company::getName))
                .collect(Collectors.toMap(
                                Company::getName,
                                company -> (long) company.getUsers().size(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        )
                );
    }

    /**
     * Zwraca pracownika, który ma najwięcej rachunków. Przy tej samej liczbie rachunków wygrywa nazwisko
     * dalsze w alfabecie.
     * Podpowiedź: Comparator.comparingInt(...).thenComparing(...).
     */
    public static Optional<User> getUserWithMostAccounts() {
        return getUserStream()
                .max(Comparator.comparingInt(
                                (User user) -> user.getAccounts().size())
                        .thenComparing(Comparator.comparing(User::getLastName).reversed())
                );
    }

    /**
     * Zwraca mapę: płeć -> średni wiek pracowników tej płci. Mapa w kolejności enuma Sex.
     */
    public static Map<Sex, Double> getAverageAgePerSex() {
        return getUserStream()
                .collect(Collectors.groupingBy(
                        User::getSex,
                        () -> new EnumMap<>(Sex.class),
                        Collectors.averagingInt(User::getAge)));
    }

    /**
     * Zwraca statystyki wieku wszystkich pracowników (count, sum, min, average, max) w jednym przejściu
     * po strumieniu.
     * Podpowiedź: strumień prymitywny i summaryStatistics().
     */
    public static IntSummaryStatistics getAgeStatistics() {
        return getUserStream()
                .mapToInt(User::getAge)
                .summaryStatistics();
    }

    /**
     * Zwraca mapę: waluta -> suma kwot na rachunkach w tej walucie. UWAGA: tutaj NIE przeliczamy na PLN,
     * sumujemy kwoty w ich własnej walucie. Mapa w kolejności enuma Currency.
     */
    public static Map<Currency, BigDecimal> getTotalBalancePerCurrency() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        () -> new EnumMap<>(Currency.class),
                        Collectors.mapping(Account::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    /**
     * Zwraca listę n najbogatszych pracowników (suma wszystkich rachunków przeliczona na PLN), od najbogatszego.
     * Format elementu listy: "Imię Nazwisko - 1234.56 PLN".
     * Przy tej samej kwocie decyduje alfabetyczna kolejność nazwiska.
     */
    public static List<String> getTopRichestUsers(final int n) {
        return getUserStream()
                .sorted(Comparator.comparing(
                                Exercises::getUserAmountInPLN)
                        .reversed()
                        .thenComparing(User::getLastName))
                .limit(n)
                .map(user -> user.getFirstName() + " " + user.getLastName() + " - " + getUserAmountInPLN(user).setScale(2, RoundingMode.HALF_UP))
                .toList();
    }

    /**
     * Zwraca mapę: uprawnienie -> liczba pracowników, którzy je posiadają. Mapa w kolejności enuma Permit.
     * Podpowiedź: jeden pracownik ma wiele uprawnień – zacznij od flatMap.
     */
    public static Map<Permit, Long> getUsersCountPerPermit() {
        return getUserStream()
                .flatMap(user -> user.getPermits().stream())
                .collect(Collectors.groupingBy(
                                permit -> permit,
                                () -> new EnumMap<>(Permit.class),
                                Collectors.counting()
                        )
                );
    }

    /**
     * Zwraca mapę trzypoziomową: nazwa firmy -> płeć -> lista napisów "Imię Nazwisko".
     * Wewnętrzna mapa ma być w kolejności enuma Sex i nie może zawierać płci, których w firmie nie ma.
     */
    public static Map<String, Map<Sex, List<String>>> getUserNamesPerCompanyAndSex() {
        return getCompanyStream()
                .collect(Collectors.toMap(
                        Company::getName,
                        company -> company.getUsers().stream()
                                .collect(Collectors.groupingBy(
                                        User::getSex,
                                        () -> new EnumMap<>(Sex.class),
                                        Collectors.mapping(user -> user.getFirstName() + " " + user.getLastName(), Collectors.toList()))
                                )));
    }

    /**
     * Zwraca histogram uprawnień jako jeden wielolinijkowy string, posortowany malejąco po liczbie wystąpień
     * (przy remisie alfabetycznie po nazwie uprawnienia). Każda linia w formacie:
     * LOAN           ################ (16)
     * czyli: nazwa uprawnienia wyrównana do 14 znaków, znaki '#' w liczbie wystąpień, na końcu liczba w nawiasie.
     * Podpowiedź: String.format("%-14s %s (%d)", ...), "#".repeat(n) i Collectors.joining("\n").
     */
    public static String getPermitsHistogram() {
        return getUsersCountPerPermit()
                .entrySet().stream()
                .sorted(Comparator.comparingLong(
                                (Map.Entry<Permit, Long> permitLongEntry) -> permitLongEntry.getValue())
                        .reversed()
                        .thenComparing(permitLongEntry -> permitLongEntry.getKey().name())
                )
                .map(permitLongEntry -> String.format("%-14s %s (%d)", permitLongEntry.getKey().name(), "#".repeat(permitLongEntry.getValue().intValue()), permitLongEntry.getValue()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Zwraca mapę: kategoria rachunku -> średnie saldo rachunków tej kategorii w PLN (2 miejsca po przecinku).
     * Średnia liczona po rachunkach, nie po pracownikach. Mapa w kolejności enuma AccountCategory.
     * Podpowiedź: Collectors.teeing – jednym kolektorem sumujesz, drugim liczysz, a w funkcji scalającej dzielisz.
     */
    public static Map<AccountCategory, BigDecimal> getAverageBalanceInPlnPerCategory() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                                account -> account.getType().getCategory(),
                                () -> new EnumMap<>(AccountCategory.class),
                                Collectors.teeing(
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                Exercises::getAccountAmountInPLN,
                                                BigDecimal::add
                                        ),
                                        Collectors.counting(),
                                        (bigDecimal, aLong) -> bigDecimal.divide(BigDecimal.valueOf(aLong), RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP)
                                )
                        )
                );
    }

    /**
     * Zwraca zbiór imion, które w danych występują więcej niż raz.
     */
    public static Set<String> getDuplicatedFirstNames() {
        return getUserStream()
                .collect(Collectors.groupingBy(
                        User::getFirstName,
                        Collectors.counting()))
                .entrySet().stream()
                .filter(stringLongEntry -> stringLongEntry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Zwraca mapę: waluta -> najbogatszy rachunek w tej walucie. Mapa w kolejności enuma Currency.
     * Zwróć uwagę na typ wartości – kolektor maxBy zwraca Optional i tak ma zostać.
     */
    public static Map<Currency, Optional<Account>> getRichestAccountPerCurrency() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                        Account::getCurrency,
                        () -> new EnumMap<>(Currency.class),
                        Collectors.maxBy(
                                Comparator.comparing(Account::getAmount))));
    }

    /**
     * Zwraca mapę: kategoria rachunku -> suma rocznych odsetek w PLN (2 miejsca po przecinku).
     * Odsetki dla jednego rachunku = saldo w PLN * oprocentowanie typu rachunku / 100
     * (AccountType.getInterestRate() zwraca wartość w procentach, np. 5.25).
     * Podpowiedź: BigDecimal.movePointLeft(2) jest bezpieczniejsze niż divide(BigDecimal.valueOf(100)).
     */
    public static Map<AccountCategory, BigDecimal> getYearlyInterestInPlnPerCategory() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                        account -> account.getType().getCategory(),
                        Collectors.collectingAndThen(
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        account -> getAccountAmountInPLN(account)
                                                .multiply(account.getType().getInterestRate().movePointLeft(2)),
                                        BigDecimal::add),
                                result -> result.setScale(2, RoundingMode.HALF_UP)
                        )));
    }

    /**
     * Zwraca mapę: przedział wiekowy -> lista napisów "Imię Nazwisko", posortowaną po kluczu.
     * Przedziały budujemy dziesiątkami: 17 lat -> "10-19", 33 lata -> "30-39", 67 lat -> "60-69".
     * W mapie nie może być pustych przedziałów.
     */
    public static TreeMap<String, List<String>> getUserNamesPerAgeBracket() {
        return getUserStream()
                .collect(Collectors.groupingBy(
                        (user -> (user.getAge() / 10 * 10) + "-" + (user.getAge() / 10 * 10 + 9)),
                        TreeMap::new,
                        Collectors.mapping(user -> user.getFirstName() + " " + user.getLastName(), Collectors.toList()))
                );

    }

    // =================================================================================
    //  CZĘŚĆ C – wyższa szkoła jazdy (★★★)
    //
    //  Tutaj przydadzą się rekordy jako klucze i "wiersze" danych, własne kolektory,
    //  Stream.iterate, takeWhile, teeing i świadomość, że stream nie zawsze jest
    //  najlepszym narzędziem. Rekordy poniżej są już gotowe – używaj ich.
    // =================================================================================

    /**
     * Zwraca strumień par firma-pracownik. Problem do rozgryzienia: po zwykłym flatMap na pracownikach
     * tracisz informację o firmie, w której pracują – a jest ona potrzebna w kolejnych zadaniach.
     * Podpowiedź: mapowanie wewnątrz flatMap.
     */
    public static Stream<CompanyUser> getCompanyUserStream() {
        return getCompanyStream()
                .flatMap(company -> company.getUsers().stream().map(user -> new CompanyUser(company, user)));
    }

    /**
     * Zwraca mapę: uprawnienie -> zbiór nazw firm, w których pracuje ktoś z tym uprawnieniem.
     * Mapa w kolejności enuma Permit, a nazwy firm posortowane alfabetycznie (TreeSet).
     * Podpowiedź: wykorzystaj getCompanyUserStream, a potem Map.entry jako parę uprawnienie-firma.
     */
    public static Map<Permit, Set<String>> getCompanyNamesPerPermit() {
        return getCompanyUserStream()
                .flatMap(companyUser -> companyUser.user.getPermits().stream()
                        .map(permit -> Map.entry(permit, companyUser.company.getName())))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        () -> new EnumMap<>(Permit.class),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(TreeSet::new))));
    }

    /**
     * Zwraca strumień "wierszy" – każdy wiersz to holding + firma + pracownik + rachunek.
     * Czteropoziomowy flatMap. Ten strumień to podstawa raportów w kolejnych zadaniach.
     */
    public static Stream<AccountRow> getAccountRowStream() {
        return holdings.stream()
                .flatMap(holding -> holding.getCompanies().stream()
                        .flatMap(company -> company.getUsers().stream()
                                .flatMap(user -> user.getAccounts().stream()
                                        .map(account -> new AccountRow(holding, company, user, account)))));
    }

    /**
     * Zwraca mapę: numer rachunku -> saldo narastające w PLN (2 miejsca po przecinku), gdzie rachunki są
     * ustawione po dacie otwarcia (przy tej samej dacie decyduje numer rachunku). Kolejność w mapie musi
     * być zachowana.
     * Saldo narastające oznacza, że wartość dla n-tego rachunku to suma sald rachunków od 1 do n.
     * <p>
     * To jedno z tych zadań, w których stream nie jest idealnym narzędziem (patrz sekcja 8 lekcji) – żeby
     * je zrobić strumieniem, potrzebujesz jednoelementowej tablicy jako akumulatora. Wolno ci jej tu użyć,
     * ale w komentarzu napisz, dlaczego takie rozwiązanie NIE zadziała na strumieniu równoległym.
     */
    public static LinkedHashMap<String, BigDecimal> getCumulativeBalanceInPlnByOpenDate() {
        BigDecimal[] cumulativeBalance = {BigDecimal.ZERO};
        return getAccountStream()
                .sorted(Comparator.comparing(Account::getOpenedAt).thenComparing(Account::getNumber))
                .collect(Collectors.toMap(
                        Account::getNumber,
                        account -> cumulativeBalance[0] = cumulativeBalance[0].add(getAccountAmountInPLN(account)).setScale(2, RoundingMode.HALF_UP),
                        (first, second) -> first,
                        LinkedHashMap::new)
                );
    }

    /**
     * Zwraca mapę: rok otwarcia rachunku -> suma sald rachunków otwartych w tym roku, w PLN
     * (2 miejsca po przecinku). Mapa posortowana po roku.
     * Podpowiedź: groupingBy z TreeMap::new oraz collectingAndThen do zaokrąglenia sumy.
     */
    public static Map<Integer, BigDecimal> getTotalBalanceInPlnPerOpeningYear() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                        account -> account.getOpenedAt().getYear(),
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                account -> getAccountAmountInPLN(account).setScale(2, RoundingMode.HALF_UP),
                                BigDecimal::add)));
    }

    /**
     * Zwraca mapę: dzień tygodnia otwarcia rachunku -> liczba rachunków otwartych w ten dzień.
     * Mapa w naturalnej kolejności dni tygodnia (poniedziałek najpierw), bez dni, w których nic nie otwarto.
     */
    public static Map<DayOfWeek, Long> getAccountsCountPerOpeningWeekDay() {
        return getAccountStream()
                .collect(Collectors.groupingBy(
                        account -> account.getOpenedAt().getDayOfWeek(),
                        () -> new EnumMap<>(DayOfWeek.class),
                        Collectors.counting()));
    }

    /**
     * Zwraca ścieżkę przełożonych: od pracownika o podanym e-mailu, przez jego przełożonego, aż do samej góry.
     * Pierwszym elementem listy jest sam pracownik. Dla nieznanego e-maila zwraca pustą listę.
     * <p>
     * Uwaga na trzy przypadki zaszyte w danych:
     * - pracownik bez przełożonego (managerEmail == null),
     * - e-mail przełożonego, którego nie ma w danych,
     * - dwie osoby, które są przełożonymi nawzajem dla siebie (cykl!) – metoda NIE może się zapętlić.
     * <p>
     * Podpowiedź: Stream.iterate(seed, hasNext, next) z Javy 9 oraz takeWhile pilnujący, żeby nie wejść
     * drugi raz na tę samą osobę.
     */
    public static List<User> getManagerChain(final String email) {
        Set<User> processedUsers = new HashSet<>();
        return Stream.iterate(
                        getUserOrNull(user -> user.getEmail().equals(email)),
                        Objects::nonNull,
                        user -> getUserOrNull(nextuser -> nextuser.getEmail().equals(user.getManagerEmail())))
                .takeWhile(processedUsers::add)
                .toList();
    }

    /**
     * Metoda pomocnicza zwracająca {@code null} jeśli {@link Exercises#getUser(Predicate)} rzuci wyjątkiem
     * {@code IllegalArgumentException: There is no such person}
     *
     */

    private static User getUserOrNull(Predicate<User> predicate) {
        try {
            return getUser(predicate);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Zwraca mapę: e-mail pracownika -> długość jego ścieżki przełożonych (czyli rozmiar listy z zadania
     * getManagerChain). Mapa posortowana po e-mailu.
     */
    public static Map<String, Integer> getOrgDepthPerEmail() {
        return getUserStream()
                .sorted(Comparator.comparing(User::getEmail))
                .collect(Collectors.toMap(
                        User::getEmail,
                        user -> getManagerChain(user.getEmail()).size(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    /**
     * Zwraca pracownika o najdłuższej ścieżce przełożonych. Przy remisie wygrywa e-mail dalszy w alfabecie.
     */
    public static Optional<User> getUserWithLongestManagerChain() {
        return getOrgDepthPerEmail().entrySet().stream()
                .max(Comparator.comparingInt((Map.Entry<String, Integer> entry) -> entry.getValue()).thenComparing(Map.Entry.<String, Integer>comparingByKey().reversed()))
                .map(stringIntegerEntry -> getUser(user -> user.getEmail().equals(stringIntegerEntry.getKey())));
    }

    /**
     * Zwraca zbiór e-maili pracowników, których domena nie zgadza się z firmą, w której pracują.
     * Reguła: e-mail powinien kończyć się na "@" + nazwa firmy małymi literami + ".com"
     * (np. pracownik firmy Fanta powinien mieć e-mail w domenie @fanta.com).
     * W danych są dwie takie osoby.
     */
    public static Set<String> findUsersWithMismatchedEmailDomain() {
        return getCompanyUserStream()
                .filter(companyUser -> !companyUser.user.getEmail().endsWith("@" + companyUser.company.getName().toLowerCase() + ".com"))
                .map(companyUser -> companyUser.user.getEmail())
                .collect(Collectors.toSet());
    }

    /**
     * Zwraca zbiór e-maili przełożonych, którzy są wpisani u pracowników, ale nie istnieją w danych
     * (czyli "wiszące" referencje).
     */
    public static Set<String> findUnknownManagerEmails() {

        return getUserStream().collect(
                Collectors.teeing(
                        Collectors.mapping(User::getEmail, Collectors.toSet()),
                        Collectors.mapping(User::getManagerEmail, Collectors.filtering(Objects::nonNull, Collectors.toSet())),
                        (emails, managerEmails) -> {
                            Set<String> result = new HashSet<>(managerEmails);
                            result.removeAll(emails);
                            return result;
                        }));
    }

    /**
     * Buduje wielolinijkowy raport – jedna linia na holding, holdingi alfabetycznie. Format linii:
     * Coca-Cola  firm: 2, pracownikow:  5, suma:        16668689.59 PLN
     * czyli: nazwa holdingu wyrównana do 10 znaków, liczba firm, liczba pracowników wyrównana do 2 znaków,
     * suma sald wszystkich pracowników w PLN wyrównana do 18 znaków.
     * <p>
     * Wymaganie: liczbę pracowników i sumę sald musisz policzyć JEDNYM kolektorem – Collectors.teeing.
     * Podpowiedź: String.format i Collectors.joining("\n").
     */
    public static String buildHoldingReport() {
        record UsersBalances(long usersNumbers, BigDecimal balances) {
        }
        return holdings.stream()
                .sorted(Comparator.comparing(Holding::getName))
                .map(holding -> {
                    UsersBalances userBalances = holding.getCompanies().stream()
                            .flatMap(company -> company.getUsers().stream())
                            .collect(Collectors.teeing(
                                    Collectors.counting(),
                                    Collectors.reducing(
                                            BigDecimal.ZERO,
                                            Exercises::getUserAmountInPLN,
                                            BigDecimal::add),
                                    UsersBalances::new)
                            );
                    return "%-10s firm:%d, pracownikow:%2d, suma:%18s PLN".formatted(
                            holding.getName(),
                            holding.getCompanies().size(),
                            userBalances.usersNumbers(),
                            userBalances.balances().setScale(2, RoundingMode.HALF_UP).toString()
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Zwraca własny, gotowy do użycia kolektor, który sumuje rachunki w PLN i zaokrągla wynik do dwóch
     * miejsc po przecinku.
     * Podpowiedź: Collector.of(supplier, accumulator, combiner, finisher). Jako pojemnik akumulatora
     * najprościej użyć jednoelementowej tablicy BigDecimal[].
     */
    public static Collector<Account, ?, BigDecimal> sumInPlnCollector() {
        return Collector.of(
                () -> new BigDecimal[]{BigDecimal.ZERO},
                (BigDecimal[] box, Account account) -> box[0] = box[0].add(getAccountAmountInPLN(account)),
                (a, b) -> new BigDecimal[]{a[0].add(b[0])},
                box -> box[0].setScale(2, RoundingMode.HALF_UP)
        );
    }

    /**
     * Zwraca mapę: miasto firmy -> suma sald rachunków jej pracowników w PLN, posortowaną po nazwie miasta.
     * Wymaganie: użyj kolektora z zadania sumInPlnCollector oraz strumienia z getAccountRowStream.
     * Podpowiedź: Collectors.mapping pozwala "przerobić" wiersz na rachunek przed przekazaniem do kolektora.
     */
    public static Map<String, BigDecimal> getTotalBalanceInPlnPerCity() {
        return getAccountRowStream()
                .collect(Collectors.groupingBy(
                                accountRow -> accountRow.company().getCity(),
                                TreeMap::new,
                                Collectors.mapping(
                                        AccountRow::account,
                                        sumInPlnCollector()
                                )
                        )
                );
    }

    /**
     * Znajduje rachunki, które zostały otwarte tego samego dnia i w tej samej walucie co inny rachunek.
     * Zwraca listę napisów w formacie "2021-01-11 EUR: 8967, 7676", posortowaną po dacie (przy tej samej
     * dacie po walucie). Grupy jednoelementowe pomijamy. W danych są cztery takie pary.
     * Podpowiedź: kluczem grupowania jest rekord DayCurrency – dlatego rekord, że ma gotowe equals i hashCode.
     */
    public static List<String> findAccountsOpenedSameDayInSameCurrency() {
        return getAccountStream()
                .collect(Collectors.groupingBy(account -> new DayCurrency(account.getOpenedAt(), account.getCurrency())))
                .entrySet().stream()
                .sorted(Comparator.comparing(dayCurrencyListEntry -> dayCurrencyListEntry.getKey().day))
                .filter(dayCurrencyListEntry -> dayCurrencyListEntry.getValue().size() > 1)
                .map(dayCurrencyListEntry ->
                        dayCurrencyListEntry.getKey().day + " "
                                + dayCurrencyListEntry.getKey().currency + ": "
                                + dayCurrencyListEntry.getValue().get(0).getNumber() + ", "
                                + dayCurrencyListEntry.getValue().get(1).getNumber())
                .toList();
    }

    /**
     * Zwraca liczbę rachunków, które strumień realnie sprawdził, zanim znalazł pierwszy pasujący do warunku.
     * To zadanie jest dowodem na leniwość strumieni: dla warunku "waluta = CHF" wynik ma być mały,
     * mimo że wszystkich rachunków jest 34.
     * Podpowiedź: peek + licznik w jednoelementowej tablicy + findFirst. Jedyne miejsce w tej pracy domowej,
     * w którym peek jest usprawiedliwiony.
     */
    public static long countCheckedAccountsUntilFirstMatch(final Predicate<Account> predicate) {
        long[] counter = {0};
        getAccountStream()
                .peek(account -> counter[0]++)
                .filter(predicate)
                .findFirst();
        return counter[0];
    }

    /**
     * Zwraca alfabetycznie posortowane nazwy firm, w których KAŻDY pracownik ma WSZYSTKIE uprawnienia z enuma
     * Permit. Firmy bez pracowników pomijamy.
     * Pytanie na koniec (odpowiedz w komentarzu): dlaczego trzeba jawnie odfiltrować firmy bez pracowników,
     * skoro allMatch na pustym strumieniu nie rzuca wyjątkiem?
     */
    public static List<String> getCompanyNamesWhereAllUsersHaveAllPermits() {
        return getCompanyStream()
                .filter(company -> !company.getUsers().isEmpty())
                .filter(company -> company.getUsers().stream()
                        .allMatch(user -> user.getPermits().containsAll(Arrays.stream(Permit.values()).toList())))
                .map(Company::getName)
                .sorted()
                .toList();

        // trzeba jawnie odfiltrować firmy bez pracowników bo allMatch() dla pustego streama też zwraca true, czyli jeśli
        // dana firma nie posiadałaby żadnego pracownika to dostalibyśmy info, że wszyscy pracownicy tej firmy mają pełne uprawnienia (co nie jest tak zupełnie nielogiczne...)
    }

    /**
     * Zwraca sumę wszystkich rachunków w PLN (2 miejsca po przecinku) policzoną na strumieniu równoległym.
     * Wynik musi być identyczny jak przy strumieniu sekwencyjnym.
     * W komentarzu odpowiedz: dlaczego reduce z BigDecimal::add jest tu bezpieczny, a dopisywanie do
     * zwykłej ArrayList w forEach nie byłoby?
     */
    public static BigDecimal getTotalBalanceInPlnParallel() {
        return getAccountStream()
                .parallel()
                .map(Exercises::getAccountAmountInPLN)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Dla chętnych: to samo co getCompanyNamesPerPermit, ale zamiast flatMap użyj mapMulti (Java 16+).
     * Wynik musi być identyczny.
     */
    public static Map<Permit, Set<String>> getCompanyNamesPerPermitWithMapMulti() {
        return getCompanyUserStream()
                .<Map.Entry<Permit, String>>mapMulti((companyUser, consumer) ->
                        companyUser.user.getPermits().forEach(
                                permit -> consumer.accept(Map.entry(permit, companyUser.company.getName()))))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        () -> new EnumMap<>(Permit.class),
                        Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(TreeSet::new))));
    }

    /**
     * Ma zwrócić napis "liczba=4, pierwsze=Adam".
     */
    //reużycie streamu, 2x terminalna
    public static String pulapkaReuzycieStreamu() {
        List<String> imiona = List.of("Adam", "Jan", "Zosia", "Jan");
        return imiona.stream()
                .collect(Collectors.teeing(
                        Collectors.counting(),
                        Collectors.reducing((prev, next) -> prev),
                        (count, firstElement) -> "liczba=" + count + ", pierwsze=" + firstElement.orElse("brak"))
                );
    }

    /**
     * Ma zwrócić mapę: imię -> nazwisko. Jeżeli to samo imię powtarza się w danych, nazwiska mają zostać
     * sklejone w kolejności występowania i oddzielone " / ", np. Jan -> "Bazuka / Nowicki".
     */
    public static Map<String, String> pulapkaKolizjaWToMap() {
        List<String> osoby = List.of("Jan Bazuka", "Zosia Psikuta", "Jan Nowicki", "Zenek Jawowy", "Zenek Biednapalka");
        return osoby.stream()
                .collect(Collectors.toMap(
                        o -> o.split(" ")[0],
                        o -> o.split(" ")[1],
                        (a, b) -> a + " / " + b));
    }

    /**
     * Ma zwrócić dziesięć pierwszych liczb naturalnych podzielnych przez 7: [7, 14, 21, ... 70].
     * UWAGA: raport celowo NIE uruchamia tej metody, bo w obecnej wersji zawiesza program. Po naprawie
     * odkomentuj odpowiednią linię w metodzie raport().
     */
    public static List<Integer> pulapkaNieskonczonyStream() {
        return Stream.iterate(1, i -> i + 1)
                .filter(i -> i % 7 == 0)
                .limit(10)
                .collect(Collectors.toList());
    }

    // =================================================================================
    //  CZĘŚĆ D – pułapki (napraw kod)
    //
    //  Każda metoda poniżej jest napisana ŹLE. Twoje zadanie:
    //   1. uruchom raport i zobacz, co się dzieje (albo nie dzieje),
    //   2. napisz w komentarzu nad metodą, dlaczego kod jest zły,
    //   3. napraw go, zachowując sens metody opisany w Javadocu.
    // =================================================================================

    /**
     * Ma zwrócić listę długości słów: [6, 6, 8, 3].
     * Podpowiedź do wyjaśnienia w komentarzu: dlaczego wynik jest PUSTY, mimo że map na pewno się wykonuje?
     * Zajrzyj do dokumentacji Stream.count().
     */
    public static List<Integer> pulapkaSideEffect() {
        List<String> slowa = List.of("stream", "lambda", "kolektor", "map");
        return slowa.stream()
                .map(String::length)
                .toList();
    }

    /**
     * Ma zwrócić sumę wieku wszystkich pracowników (692). Wynik jest poprawny, ale kod bez potrzeby pakuje
     * i rozpakowuje int w Integer przy każdym elemencie. Popraw go tak, żeby nie było boxingu.
     */
    public static int pulapkaBoxing() {
        List<Integer> wiek = List.of(17, 33, 18, 46, 67, 33, 29, 33, 18, 21, 50, 37, 45, 29, 29, 64, 33, 28, 22, 40);
        return wiek.stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Ma zwrócić słowo dłuższe niż 100 znaków, a gdy takiego nie ma – napis "brak takiego slowa".
     */
    public static String pulapkaOptionalGet() {
        List<String> slowa = List.of("stream", "lambda", "kolektor");
        return slowa.stream()
                .filter(s -> s.length() > 100)
                .findFirst()
                .orElse("brak takiego slowa");
    }

    private static void raport() {
        naglowek("CZESC A - podstawy");
        wynik("A1  getHoldingsWhereAreCompanies", Exercises::getHoldingsWhereAreCompanies);
        wynik("A2  getHoldingNames", Exercises::getHoldingNames);
        wynik("A3  getHoldingNamesAsString", Exercises::getHoldingNamesAsString);
        wynik("A4  getCompaniesAmount", Exercises::getCompaniesAmount);
        wynik("A5  getAllUserAmount", Exercises::getAllUserAmount);
        wynik("A6  getAllCompaniesNamesAsLinkedList", Exercises::getAllCompaniesNamesAsLinkedList);
        wynik("A7  getAccountAmountInPLN(1000 EUR)", () -> getAccountAmountInPLN(
                new Account(AccountType.ROR1, "8967", new BigDecimal("1000"), Currency.EUR, LocalDate.of(2021, 1, 11))));
        wynik("A8  getUsersForPredicate(wiek > 40)", () -> getUsersForPredicate(u -> u.getAge() > 40));
        efekt("A9  executeForEachCompany(wypisz nazwe)", () -> executeForEachCompany(c -> System.out.println("    " + c.getName())));
        wynik("A10 getRichestWoman", Exercises::getRichestWoman);
        wynik("A11 getFirstNCompany(3)", () -> getFirstNCompany(3));
        wynik("A12 getUserPerCompany", Exercises::getUserPerCompany);
        wynik("A13 getUser(imie = Kasia)", () -> getUser(u -> u.getFirstName().equals("Kasia")));
        wynik("A14 createAccountsMap().size()", () -> createAccountsMap() == null ? null : createAccountsMap().size());
        wynik("A15 getUserNames", Exercises::getUserNames);
        efekt("A16 showAllUser", Exercises::showAllUser);
        wynik("A17 getCurenciesSet", Exercises::getCurenciesSet);
        wynik("A18 getCompanyStream().count()", () -> getCompanyStream() == null ? null : getCompanyStream().count());
        wynik("A19 getUserStream().count()", () -> getUserStream() == null ? null : getUserStream().count());
        wynik("A20 getAccoutStream().count()", () -> getAccountStream() == null ? null : getAccountStream().count());

        naglowek("CZESC B - Collectors");
        wynik("B1  getCompaniesCountPerCountry", Exercises::getCompaniesCountPerCountry);
        wynik("B2  getCompanyNamesPerRegionAndCountry", Exercises::getCompanyNamesPerRegionAndCountry);
        wynik("B3  getTotalBalanceInPlnPerHolding", Exercises::getTotalBalanceInPlnPerHolding);
        wynik("B4  getAccountNumbersPerType", Exercises::getAccountNumbersPerType);
        wynik("B5  partitionUserNamesByAge(30)", () -> partitionUserNamesByAge(30));
        wynik("B6  getUsersCountPerCompanyDescending", Exercises::getUsersCountPerCompanyDescending);
        wynik("B7  getUserWithMostAccounts", Exercises::getUserWithMostAccounts);
        wynik("B8  getAverageAgePerSex", Exercises::getAverageAgePerSex);
        wynik("B9  getAgeStatistics", Exercises::getAgeStatistics);
        wynik("B10 getTotalBalancePerCurrency", Exercises::getTotalBalancePerCurrency);
        wynik("B11 getTopRichestUsers(5)", () -> getTopRichestUsers(5));
        wynik("B12 getUsersCountPerPermit", Exercises::getUsersCountPerPermit);
        wynik("B13 getUserNamesPerCompanyAndSex", Exercises::getUserNamesPerCompanyAndSex);
        wynik("B14 getPermitsHistogram", Exercises::getPermitsHistogram);
        wynik("B15 getAverageBalanceInPlnPerCategory", Exercises::getAverageBalanceInPlnPerCategory);
        wynik("B16 getDuplicatedFirstNames", Exercises::getDuplicatedFirstNames);
        wynik("B17 getRichestAccountPerCurrency", Exercises::getRichestAccountPerCurrency);
        wynik("B18 getYearlyInterestInPlnPerCategory", Exercises::getYearlyInterestInPlnPerCategory);
        wynik("B19 getUserNamesPerAgeBracket", Exercises::getUserNamesPerAgeBracket);

        naglowek("CZESC C - wyzsza szkola jazdy");
        wynik("C1  getCompanyUserStream().count()", () -> getCompanyUserStream() == null ? null : getCompanyUserStream().count());
        wynik("C2  getCompanyNamesPerPermit", Exercises::getCompanyNamesPerPermit);
        wynik("C3  getAccountRowStream().count()", () -> getAccountRowStream() == null ? null : getAccountRowStream().count());
        wynik("C4  getCumulativeBalanceInPlnByOpenDate", Exercises::getCumulativeBalanceInPlnByOpenDate);
        wynik("C5  getTotalBalanceInPlnPerOpeningYear", Exercises::getTotalBalanceInPlnPerOpeningYear);
        wynik("C6  getAccountsCountPerOpeningWeekDay", Exercises::getAccountsCountPerOpeningWeekDay);
        wynik("C7  getManagerChain(amadeusz.mocarz@nescafe.com)", () -> getManagerChain("amadeusz.mocarz@nescafe.com"));
        wynik("C7  getManagerChain(zenek.biednapalka@nescafe.com) [cykl]", () -> getManagerChain("zenek.biednapalka@nescafe.com"));
        wynik("C7  getManagerChain(nieznany@nikt.com)", () -> getManagerChain("nieznany@nikt.com"));
        wynik("C8  getOrgDepthPerEmail", Exercises::getOrgDepthPerEmail);
        wynik("C9  getUserWithLongestManagerChain", Exercises::getUserWithLongestManagerChain);
        wynik("C10 findUsersWithMismatchedEmailDomain", Exercises::findUsersWithMismatchedEmailDomain);
        wynik("C11 findUnknownManagerEmails", Exercises::findUnknownManagerEmails);
        wynik("C12 buildHoldingReport", Exercises::buildHoldingReport);
        wynik("C13 sumInPlnCollector", () -> sumInPlnCollector() == null ? null : "kolektor gotowy");
        wynik("C14 getTotalBalanceInPlnPerCity", Exercises::getTotalBalanceInPlnPerCity);
        wynik("C15 findAccountsOpenedSameDayInSameCurrency", Exercises::findAccountsOpenedSameDayInSameCurrency);
        wynik("C16 countCheckedAccountsUntilFirstMatch(CHF)", () -> countCheckedAccountsUntilFirstMatch(a -> a.getCurrency() == Currency.CHF));
        wynik("C17 getCompanyNamesWhereAllUsersHaveAllPermits", Exercises::getCompanyNamesWhereAllUsersHaveAllPermits);
        wynik("C18 getTotalBalanceInPlnParallel", Exercises::getTotalBalanceInPlnParallel);
        wynik("C19 getCompanyNamesPerPermitWithMapMulti", Exercises::getCompanyNamesPerPermitWithMapMulti);

        naglowek("CZESC D - pulapki");
        wynik("D1  pulapkaReuzycieStreamu", Exercises::pulapkaReuzycieStreamu);
        wynik("D2  pulapkaKolizjaWToMap", Exercises::pulapkaKolizjaWToMap);
        // System.out.println("- D3  pulapkaNieskonczonyStream:");
        // System.out.println("    (pominiete - obecna wersja zawiesza program, odkomentuj po naprawie)");
        wynik("D3  pulapkaNieskonczonyStream", Exercises::pulapkaNieskonczonyStream);
        wynik("D4  pulapkaSideEffect", Exercises::pulapkaSideEffect);
        wynik("D5  pulapkaBoxing", Exercises::pulapkaBoxing);
        wynik("D6  pulapkaOptionalGet", Exercises::pulapkaOptionalGet);
    }

    private static void naglowek(final String tytul) {
        System.out.println();
        System.out.println("=".repeat(78));
        System.out.println("  " + tytul);
        System.out.println("=".repeat(78));
    }

    private static void wynik(final String etykieta, final Supplier<Object> zadanie) {
        System.out.println("- " + etykieta + ":");
        try {
            System.out.println(sformatuj(zadanie.get()));
        } catch (Exception e) {
            System.out.println("    !! " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + e.getMessage()));
        }
    }

    // =================================================================================
    //  RAPORT – uruchamiarka zadań. Tutaj nic nie musisz zmieniać (poza jedną linią w D3).
    //  Odpal main i porównaj wynik z plikiem README.md.
    //  "(brak wyniku - zadanie do zrobienia)" oznacza, że metoda nadal zwraca null.
    // =================================================================================

    private static void efekt(final String etykieta, final Runnable zadanie) {
        System.out.println("- " + etykieta + ":");
        try {
            zadanie.run();
        } catch (Exception e) {
            System.out.println("    !! " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + e.getMessage()));
        }
    }

    private static String sformatuj(final Object wartosc) {
        if (wartosc == null) {
            return "    (brak wyniku - zadanie do zrobienia)";
        }
        if (wartosc instanceof Map<?, ?> mapa) {
            if (mapa.isEmpty()) {
                return "    (pusta mapa)";
            }
            Stream<? extends Map.Entry<?, ?>> wpisy = mapa.entrySet().stream();
            if (!(mapa instanceof LinkedHashMap || mapa instanceof SortedMap || mapa instanceof EnumMap)) {
                wpisy = wpisy.sorted(Comparator.comparing(e -> String.valueOf(e.getKey())));
            }
            return wpisy.map(e -> "    " + e.getKey() + " -> " + e.getValue())
                    .collect(Collectors.joining("\n"));
        }
        if (wartosc instanceof Collection<?> kolekcja) {
            if (kolekcja.isEmpty()) {
                return "    (pusta kolekcja)";
            }
            Stream<?> elementy = kolekcja.stream();
            if (kolekcja instanceof Set && !(kolekcja instanceof SortedSet || kolekcja instanceof LinkedHashSet)) {
                elementy = elementy.sorted(Comparator.comparing(String::valueOf));
            }
            return elementy.map(e -> "    " + e).collect(Collectors.joining("\n"));
        }
        if (wartosc instanceof String tekst && tekst.contains("\n")) {
            return tekst.lines().map(l -> "    " + l).collect(Collectors.joining("\n"));
        }
        return "    " + wartosc;
    }

    public record CompanyUser(Company company, User user) {
    }

    public record AccountRow(Holding holding, Company company, User user, Account account) {
    }

    public record DayCurrency(LocalDate day, Currency currency) {
    }
}
