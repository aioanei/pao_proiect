# Proiect PAO - Simulator de Bursă (Trading Terminal)

Acest proiect reprezintă implementarea cerințelor pentru evaluarea la disciplina PAO (Programare Avansată pe Obiecte). Sistemul simulează o piață bursieră în timp real (prin intermediul unui thread separat) unde investitorii pot cumpăra, vizualiza și tranzacționa active financiare.

---

## Definirea Sistemului

### Lista celor 8+ tipuri de obiecte (clase) implementate:
1. **`Asset`** - Clasă de bază (abstractă) pentru toate activele tranzacționabile.
2. **`Stock`** - Acțiuni clasice, moștenește `Asset`. Adaugă companie și dividende.
3. **`Crypto`** - Criptomonede, moștenește `Asset`. Adaugă rețeaua blockchain.
4. **`User`** - Clasă de bază pentru utilizatorii sistemului.
5. **`Investor`** - Investitorul activ, moștenește `User`. Conține portofoliul și balanța.
6. **`PortfolioItem`** - O entitate care reține informații despre ce activ deține un utilizator (cantitate, preț de achiziție).
7. **`Transaction`** - Istoricul unei tranzacții (conține timestamp, preț, tip).
8. **`Market`** - Piața agregată; gestionează activele disponibile și motorul de simulare a prețurilor.
9. **`TransactionType`** - Enum pentru BUY / SELL.

### Lista celor 10+ acțiuni/interogări:
1. **Afișarea activelor de pe piață**, ordonate crescător după preț (utilizează colecție sortată automat).
2. **Înregistrarea automată a unui investitor** și atribuirea unui portofoliu/buget inițial.
3. **Cumpărarea (Buy)** de active financiare și calcularea prețului mediu de achiziție.
4. **Vânzarea (Sell)** de active din portofoliu cu validare de cantități.
5. **Afișarea detaliată a portofoliului**, cu conversia valorii la adusă timpul real al pieței.
6. **Depunerea de fonduri** externe in contul investitorului.
7. **Afișarea istoricului tranzacțiilor** (toate acțiunile de BUY/SELL).
8. **Interogarea celui mai scump activ existent** la un moment dat.
9. **Afișarea istoricului de preț** pentru a vedea cum a evoluat un activ de la începutul programului.