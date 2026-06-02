# Proiect PAO - Simulator de Bursa (Trading Terminal)

---

## 1. Definirea Sistemului

### Tipurile de obiecte implementate (9 clase/enum-uri)
In respectarea cerintei de a avea minim 8 tipuri de obiecte, au fost implementate urmatoarele:

1. **`Asset`** - Clasa de baza (abstracta) pentru toate activele tranzactionabile.
2. **`Stock`** - Actiuni (mosteneste `Asset`), adauga informatii despre companie si dividende.
3. **`Crypto`** - Criptomonede (mosteneste `Asset`), adauga informatii despre reteaua de blockchain.
4. **`User`** - Clasa de baza pentru orice persoana din sistem.
5. **`Investor`** - Investitor activ (mosteneste `User`). Contine un portofoliu si o balanta financiara.
6. **`PortfolioItem`** - O entitate ce retine ce activ detine un utilizator in portofoliu, cantitatea lui si pretul mediu de achizitie.
7. **`Transaction`** - Reprezinta o inregistrare in istoricul tranzactiilor (contine timestamp, pret, tip, suma, etc).
8. **`Market`** - Clasa ce gestioneaza ansamblul ofertelor de pe piata si mecanismul de modificare a preturilor.
9. **`TransactionType`** - Enumerare ce defineste tipul unei operatiuni (BUY / SELL).

### Lista celor 14 actiuni / interogari posibile
Aplicatia acopera (si depaseste) cerinta de a avea minim 10 actiuni si interogari:

1. **Afisarea activelor de pe piata**, sortate crescator dupa pret.
2. **Afisarea portofoliului**, aratand profitul raportat la preturile live.
3. **Cumpararea (Buy)** de active cu validarea balantei.
4. **Vanzarea (Sell)** de active din portofoliu.
5. **Depunerea (Deposit)** fondurilor.
6. **Retragerea (Withdraw)** fondurilor.
7. **Afisarea istoricului tranzactiilor** (BUY/SELL).
8. **Interogarea celui mai scump activ** de pe piata.
9. **Afisarea istoricului de pret** pentru un anumit activ in timp (se salveaza istoric pentru preturile care s-au schimbat).
10. **Cautarea unui activ** dupa simbol (ex: AAPL).
11. **Filtrarea pietei: Doar Actiuni**.
12. **Filtrarea pietei: Doar Criptomonede**.
13. **Generarea automata a fluctuatiei preturilor** folosind Thread-uri in background (Live Market).
14. **Inregistrarea automata a investitorului** cu sold initial.

---

## 2. Implementarea Cerintelor Tehnice

Structura si arhitectura aplicatiei acopera toate cerintele specifice:

* **Clase simple & Incapsulare:** Toate clasele (inclusiv extensiile) folosesc `protected` sau `private`, iar prelucrarea si alocarea se face via `getters/setters`.
* **Colectii diferite (inclusiv una sortata):**
  * `TreeSet<Asset>`: Se afla in interiorul `Market`. Asigura ca elementele sunt constant sortate dupa pret (datorita utilizarii `Comparable` pe clasa de baza `Asset`).
  * `Map<String, PortfolioItem>`: Interfata cheie-valoare din interiorul agragatorului `Investor`. Key String-ul este simbolul companiei si asigura ca la cumparare complexitatea verificarilor si update-urilor portofoliului e de `O(1)`.
  * `List<Transaction>` si `List<Double>`: Folosite pentru stocarea statica a istoricului actiunilor pe cont, respectiv evolutia ratei actiunilor.
* **Mostenire si Polimorfism:** Relatia de agregare a pietelor este strict construita pe polimorfism. Investitorul cumpara si piata stocheaza `Asset`, care sub acoperire poarta insemnele specifice fie a `Stock` (dividende) fie a `Crypto` (blockchain network). Aceeasi ierarhie a fost formata la sistemul de autentificare: `User` extins cu unelte financiare formand `Investor`.
* **Clase Serviciu:** Centralizate in `TradingService`, decuplate de partea CLI afisata pe ecran in meniu.
* **Control Main:** Intrarea principala de loop pentru utilizator `(while-Scanner)` unde acesta alege una dintre cele peste 13 functii implementate.

---

## 3. Extindere Etapa 2 - JDBC, baza de date relationala si audit

Persistenta este implementata prin JDBC, folosind o baza de date relationala H2 configurata in `database.properties`.
Schema este creata automat la pornirea aplicatiei, iar fisierele generate se afla in directorul `data/`.

### Servicii singleton CRUD

Au fost adaugate servicii repository singleton, bazate pe interfata generica `CrudRepository<T, K>`, pentru urmatoarele clase:

1. `StockRepository` - CRUD pentru `Stock`.
2. `CryptoRepository` - CRUD pentru `Crypto`.
3. `InvestorRepository` - CRUD pentru `Investor`.
4. `TransactionRepository` - CRUD pentru `Transaction`.

In plus, `PortfolioItemRepository` salveaza portofoliul investitorului in tabela `portfolio_items`.

### Serviciu de audit

`AuditService` scrie in `data/audit.csv` fiecare actiune executata din aplicatie, cu structura:

```csv
nume_actiune,timestamp
```

### Rulare

Cu Maven instalat:

```powershell
mvn compile exec:java
```

Fara Maven, descarcati driverul H2 in `lib/h2-2.2.224.jar`, apoi rulati:

```powershell
javac -cp "lib/h2-2.2.224.jar" -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "bin;lib/h2-2.2.224.jar" org.trading.Main
```
