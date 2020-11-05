package com.example.smarthomesystem;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Clasa testata si construita pe desktop, portata pe android ulterior
 * proceseaza comenzile vocale sub forma de text poate aici daca as gasi o alta
 * abordare, functioneaza ok asa cum e dar poate imi vine o idee mai buna si mai
 * scalabila decat acum
 * <p>
 * practic tot ce face clasa asta se comprima in functia {@link #tradu(String)}
 * care primeste orice spune utilizatorul si returneaza o {@link Intentie} ce
 * contine date concrete referitoare la comenzile impuse de utilizator
 *
 * @author Manel
 * @since 06/02/2020-16:27
 */
public class Traducator {

    public Traducator() {
        emptyIntent = new Intentie();
    }

    public int LIMBA = App.LIMBA_ROMANA;

    private String COMENZI[][] = {
            {"porneste", "aprinde", "urneste"},
            {"opreste", "inceteaza", "stinge"},
            {"porneste", "aprinde", "urneste", "aprinzi", "culoarea", "luminile", "becurile", "ledurile", "led-urile", "leduri", "led-uri", "lumina", "becu", "culoarea", "culoare"},
            {"seteaza", "pune", "alarma", "programeaza", "trezeste"},
            {"galben", "galbena", "galbene", "mov", "mova", "rosu", "rosii", "rosie", "portocaliu", "portocalie", "portocalii", "portocaliul", "verde", "verzi", "albastru", "albastrul", "albastra", "albastre"},
            {"pune", "baga"},
            {"porneste", "aprinde", "urneste"},
            {"opreste", "inceteaza", "stinge"}
    };
    private String COMPLEMENTI[][] = {
            {"luminile", "becurile", "ledurile", "led-urile", "leduri", "led-uri", "lumina", "becu"},
            {"luminile", "becurile", "ledurile", "led-urile", "leduri", "led-uri", "lumina", "becu"},
            {"luminile", "becurile", "ledurile", "led-urile", "leduri", "led-uri", "lumina", "becu", "culoarea", "culoare", "galben", "galbena", "galbene", "mov", "mova",
                    "rosu", "rosii", "rosie", "portocaliu", "portocalie", "portocalii", "portocaliul", "verde", "verzi", "albastru", "albastrul", "albastra", "albastre"},
            {"alarma", "in", "la", "peste", "intr", "intru"},
            {"%"},
            {""},
            {"lumina_de_lucru", "lumina_mare"},
            {"lumina_de_lucru", "lumina_mare"}
    };
    int COMPLEMENTI_NECESARI[] = {
            1, 1, 2, 2, 1, 0, 1,1
    };
    boolean REGULAR_EXPRESSION[] = {
            false, false, false, false, true, false, false,false
    };

    boolean complementiIndepliniti(int comanda, int nrComplementi) {
        return (COMPLEMENTI_NECESARI[comanda] == nrComplementi);
    }

    private Intentie translateEnglish(String sentence) {
        return null;
    }

    private int cautaComanda(List<String> cuvinte) {
        for (String cuvant : cuvinte) {
            for (int i = 0; i < COMENZI.length; i++) {
                for (int j = 0; j < COMENZI[i].length; j++) {
                    if (COMENZI[i][j].equals(cuvant)) {
                        return i;
                    }
                }
            }
        }
        return App.COMANDA_NESUPORTATA;
    }

    boolean areComplement(int comanda) {
        try {
            String primulComplement = COMPLEMENTI[comanda][0];
            return true;
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    List<Integer> cautaComenzi(List<String> cuvinte) {
        List<Integer> comenzi = null;
        for (String cuvant : cuvinte) {
            for (int i = 0; i < COMENZI.length; i++) {
                for (int j = 0; j < COMENZI[i].length; j++) {
                    if (COMENZI[i][j].equals(cuvant)) {
                        if (comenzi == null) {
                            comenzi = new ArrayList<>();
                        }
                        if (!comenzi.contains(i)) {
                            comenzi.add(i);
                        }
                    }
                }
            }
        }
        return comenzi;
    }

    static Intentie emptyIntent;

    public static Intentie getEmptyIntent() {
        return emptyIntent;
    }

    class Intentie {

        PachetIntentie[] pachet;

        Intentie(PachetIntentie... pachet) {
            this.pachet = pachet;
        }

        private List<PachetIntentie> lista;

        Intentie() {
            this.lista = new ArrayList<>();
        }

        void add(PachetIntentie pachet) {
            lista.add(pachet);
        }

        void finalizare() {
            pachet = new PachetIntentie[lista.size()];
            for (int i = 0; i < lista.size(); i++) {
                pachet[i] = lista.get(i);
            }
            lista.clear();
        }
    }

    class Alarma {

        int tip;
        /**
         * pentru alarmele de tip ALARMA_IN se folosesc tot astea, practic pt
         * alarmele ALARMA_IN ele o sa retina in cate ore si respectiv in cate
         * minute o sa sune alarma iar pentru alarmele de tin ALARMA_LA o sa
         * retina pur si simplu ora si minutul la care utilizatorul specifica ca
         * vrea sa fie alarma
         */
        int ora, minut;
    }

    class Piesa {

        String nume;

        Piesa(String nume) {
            this.nume = nume;
        }
    }

    class PachetIntentie {

        int comanda;
        String detalii[];
        Alarma alarma;
        Piesa piesa;
        int intensitate;

        public PachetIntentie(int comanda, String detalii[]) {
            this.comanda = comanda;
            this.detalii = detalii;
        }

        public void print() {
            Log.println(Log.ASSERT, "PACHET", comanda + " " + Arrays.toString(detalii));
        }
    }

    private String[] cautaComplementi(int comanda, List<String> cuvinte) {
        List<String> cuv = null;
        StringBuilder builder = new StringBuilder();
        for (String s : cuvinte) {
            builder.append(s).append(" ");
        }
        for (String cuvant : cuvinte) {
            for (int i = 0; i < COMPLEMENTI[comanda].length; i++) {
                //print(COMPLEMENTI[comanda][i]+" "+cuvant);
                if (COMPLEMENTI[comanda][i].equals(cuvant)) {
                    if (cuv == null) {
                        cuv = new ArrayList<>();
                    }
                    if (!cuv.contains(cuvant)) {
                        cuv.add(cuvant);
                    }
                }
            }
        }
        if (REGULAR_EXPRESSION[comanda]) {
            for (int i = 0; i < COMPLEMENTI[comanda].length; i++) {
                //.* *masina de spalat *.*
                if (comanda == App.COMANDA_SETEAZA_INTENSITATE_CULOARE) {
                    for (String x : cuvinte) {
                        if (x.contains("%")) {
                            if (cuv == null) {
                                cuv = new ArrayList<>();
                            }
                            if (!cuv.contains(COMPLEMENTI[comanda][i])) {
                                cuv.add(COMPLEMENTI[comanda][i]);
                            }
                        }
                    }
                } else {
                    if (Pattern.matches(".* *" + COMPLEMENTI[comanda][i] + "*.*", builder.toString())) {
                        if (cuv == null) {
                            cuv = new ArrayList<>();
                        }
                        if (!cuv.contains(COMPLEMENTI[comanda][i])) {
                            cuv.add(COMPLEMENTI[comanda][i]);
                            print("ce dracu e cu cacatu asata");
                        }
                    }
                }
            }
        }

        if (COMPLEMENTI_NECESARI[comanda] == 0) {
            if (cuv == null) {
                cuv = new ArrayList<>();
            }
            cuv.clear();
            for (int i = 1; i < cuvinte.size(); i++) {
                if (!cuvinte.get(i).equals("piesa")) {
                    cuv.add(cuvinte.get(i));
                }
            }
        }

        if (cuv == null) {
            throw new NullPointerException("Nu am gasit niciun complement bun pt comanda " + comanda);
        }

        String ar[] = new String[cuv.size()];
        for (int i = 0; i < cuv.size(); i++) {
            ar[i] = cuv.get(i);
        }
        return ar;
    }

    static int conversieLaCifra(String cifra) {
        //<editor-fold desc="body" defaultstate="collapsed">
        switch (cifra) {
            case "unu":
                return (1);
            case "un":
                return (1);
            case "doi":
                return (2);
            case "doua":
                return (2);
            case "trei":
                return (3);
            case "patru":
                return (4);
            case "cinci":
                return (5);
            case "sase":
                return (6);
            case "sapte":
                return (7);
            case "opt":
                return (8);
            case "noua":
                return (9);
            case "zece":
                return (10);
            case "jumate":
                return (30);
            case "jumatate":
                return (30);
            case "sfert":
                return (15);
        }
        //</editor-fold>
        return -1;
    }

    boolean intValid(String x) {
        int in;
        try {
            in = Integer.parseInt(x);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    int getAlarmaType(String ar[], String[] alarmaIN_TYPE, String alarmaLA_TYPE) {
        for (String word : ar) {
            for (String in_type : alarmaIN_TYPE) {
                if (word.equals(in_type)) {
                    return App.ALARMA_IN;
                }
            }
            if (word.equals(alarmaLA_TYPE)) {
                return App.ALARMA_LA;
            }
        }
        return -3;
    }

    void insertTimp(String prop, PachetIntentie pachet) {
        //print(Pattern.matches("^(\\w+)*( )*(0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]( \\w+)*( )*$", "112:38"));
        //print(Pattern.matches("^(0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]$","12:03"));
        if (prop.contains(":")) {
            prop = prop.replace(":", " ");
        }
        if (prop.contains("de")) {
            prop = prop.replace("de", "");
        }
        String ar[] = prop.split(" ");
        int del = -1;
        for (int i = 0; i < ar.length; i++) {
            if (ar[i].equals("")) {
                System.out.println("aVEM GOLACIUNE");
                del = i;
                break;
            }
        }
        if (del != -1) {
            for (int i = del; i < ar.length - 1; i++) {
                ar[i] = ar[i + 1];
            }
        }
        for (int i = 0; i < ar.length; i++) {
            int conversie = conversieLaCifra(ar[i]);
            if (conversie != -1) {
                ar[i] = Integer.toString(conversie);
            }
        }
        String alarmaIN[] = new String[]{"in", "peste", "intr", "intru"};
        String alarmaLA = "la";
        String oreMarkers = "ore ora";

        prop = Arrays.toString(ar);
        int tipAlarma = getAlarmaType(ar, alarmaIN, alarmaLA);
        if (tipAlarma == App.ALARMA_IN) {
            print("ALARMA IN: " + prop);
            int ore = 0, minute = 0;
            for (int i = 0; i < ar.length - 1; i++) {
                if (ar[i].equals("o") && ar[i + 1].equals("ora")) {
                    ar[i] = "1";
                }
                if (intValid(ar[i])) {
                    if (ore == 0 && oreMarkers.contains(ar[i + 1])) {
                        ore = Integer.valueOf(ar[i]);
                    } else if (minute == 0) {
                        minute = Integer.valueOf(ar[i]);
                    }
                }
            }
            if (ore != 0 && intValid(ar[ar.length - 1])) {
                minute = Integer.valueOf(ar[ar.length - 1]);
            }
            //long milis=TimeUnit.HOURS.toMillis(ore)+TimeUnit.MINUTES.toMillis(minute);
            //print("ore :"+ore+" minute: "+minute);
            pachet.alarma = new Alarma();
            pachet.alarma.ora = ore;
            pachet.alarma.minut = minute;
            pachet.alarma.tip = App.ALARMA_IN;
        } else if (tipAlarma == App.ALARMA_LA) {
            print("ALARMA LA:" + prop);

            int ore = 0, minute = 0;
            for (int i = 0; i < ar.length - 1; i++) {

                if (intValid(ar[i])) {
                    if (ore == 0) {
                        ore = Integer.valueOf(ar[i]);
                    } else if (minute == 0) {
                        minute = Integer.valueOf(ar[i]);
                    }
                }
            }
            if (ore != 0 && intValid(ar[ar.length - 1])) {
                minute = Integer.valueOf(ar[ar.length - 1]);
            } else if (ore == 0) {
                if (intValid(ar[ar.length - 1])) {
                    ore = Integer.valueOf(ar[ar.length - 1]);
                }
            }

            for (int i = 0; i < ar.length; i++) {
                if ((ar[i].equals("seara") || prop.contains("dupa masa")
                        || prop.contains("ziua")) && ore <= 12) {
                    ore += 12;
                }
            }
            ore %= 24;
            //long milis=TimeUnit.HOURS.toMillis(ore)+TimeUnit.MINUTES.toMillis(minute);
            //print("ore :"+ore+" minute: "+minute);
            pachet.alarma = new Alarma();
            pachet.alarma.ora = ore;
            pachet.alarma.minut = minute;
            pachet.alarma.tip = App.ALARMA_LA;
        }
    }

    int cazSpecial(int comanda, String complementi[], String prop) {
        //<editor-fold defaultstate="collapsed" desc="alarma">
        String oreStrings = "ore ora";
        String minutStrings = "minut minute";
        if (comanda == App.COMANDA_ALARMA) {
            if (complementi.length == 3) {
                boolean gasitOra = false;
                boolean gasitMinut = false;
                for (int i = 0; i < 3; i++) {
                    if (oreStrings.contains(complementi[i])) {
                        gasitOra = true;
                    }
                    if (minutStrings.contains(complementi[i])) {
                        gasitMinut = true;
                    }
                }
                if (gasitMinut && gasitOra) {
                    return App.COMANDA_ALARMA;
                }

            }
        }
        //</editor-fold>
        if (comanda == App.COMANDA_SETEAZA_INTENSITATE_CULOARE && prop.contains("%")) {
            return App.COMANDA_SETEAZA_INTENSITATE_CULOARE;
        }
        if (comanda == App.COMANDA_MUZICA) {
            return App.COMANDA_MUZICA;
        }
        return App.COMANDA_NESUPORTATA;
    }

    boolean contineCuv(String ar[], String[] cuvinte) {
        for (String x : ar) {
            for (String cuvant : cuvinte) {
                if (x.equals(cuvant)) {
                    return true;
                }
            }
        }
        return false;
    }

    void insertIntensitateSiCuloare(String ar[], PachetIntentie pachet) {
        String culori[] = new String[]{"galben", "galbena", "galbene", "mov", "mova", "rosu", "rosii", "rosie", "portocaliu", "portocalie", "portocalii", "verde", "verzi", "albastru", "albastra", "albastre"};
        for (int i = 0; i < ar.length; i++) {
            if (ar[i].contains("%")) {
                pachet.intensitate = Integer.valueOf(ar[i].replace("%", ""));
            }
            for (String x : culori) {
                if (x.equals(ar[i])) {
                    pachet.detalii[0] = x;
                }
            }
        }
    }

    void insertMuzica(String ar[], PachetIntentie pachet) {
        String x = "";
        for (int i = 1; i < ar.length; i++) {
            if (!ar[i].equals("piesa")) {
                x += ar[i] + (i < ar.length - 1 ? " " : "");
            }
        }
        pachet.piesa = new Piesa(x);
    }

    private Intentie traducereRomana(String prop) {

        StringTokenizer tokenizer = new StringTokenizer(prop);
        List<String> cuvinte = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            cuvinte.add(tokenizer.nextToken());
        }
        //<editor-fold desc="alte cazuri speciale" defaultstate="collapsed">

        if (prop.contains(" jumate de ora") || prop.contains(" jumatate de ora")) {
            cuvinte.remove("jumate");
            cuvinte.remove("jumatate");
            cuvinte.remove("de");
            cuvinte.remove("ora");
            cuvinte.add("0");
            cuvinte.add("ore");
            cuvinte.add("30");
            cuvinte.add("minute");
            prop = cuvinte.toString();
            String x = "";
            for (int i = 0; i < cuvinte.size(); i++) {
                x += cuvinte.get(i) + " ";
            }
            prop = x;
            print("noua prop " + prop);
        }
        if (prop.contains(" in sfert de ora")) {
            cuvinte.remove("sfert");
            cuvinte.remove("de");
            cuvinte.remove("ora");
            cuvinte.add("0");
            cuvinte.add("ore");
            cuvinte.add("15");
            cuvinte.add("minute");
            String x = "";
            for (int i = 0; i < cuvinte.size(); i++) {
                x += cuvinte.get(i) + " ";
            }
            prop = x;
            print("noua prop " + prop);
        }
        if (prop.contains(" un sfert de ora")) {
            cuvinte.remove("un");
            cuvinte.remove("sfert");
            cuvinte.remove("de");
            cuvinte.remove("ora");
            cuvinte.add("0");
            cuvinte.add("ore");
            cuvinte.add("15");
            cuvinte.add("minute");
            String x = "";
            for (int i = 0; i < cuvinte.size(); i++) {
                x += cuvinte.get(i) + " ";
            }
            prop = x;

            print("noua prop " + prop);
        } else if (prop.contains(" un sfert")) {
            cuvinte.remove("un");
            String x = "";
            for (int i = 0; i < cuvinte.size(); i++) {
                x += cuvinte.get(i) + " ";
            }
            prop = x;
            print("noua prop " + prop);
        }
        if (prop.contains("intrun sfert de ora")) {
            cuvinte.remove("intrun");
            cuvinte.remove("sfert");
            cuvinte.remove("de");
            cuvinte.remove("ora");
            cuvinte.add("in");
            cuvinte.add("0");
            cuvinte.add("ore");
            cuvinte.add("15");
            cuvinte.add("minute");

            String x = "";
            for (int i = 0; i < cuvinte.size(); i++) {
                x += cuvinte.get(i) + " ";
            }
            prop = x;
            print("noua prop " + prop);
        }
        if (prop.contains("lumina de lucru")) {
            cuvinte.remove("lumina");
            cuvinte.remove("de");
            cuvinte.remove("lucru");
            cuvinte.add("lumina_de_lucru");
        }
        if (prop.contains("lumina mare")) {
            cuvinte.remove("lumina");
            cuvinte.remove("mare");
            cuvinte.add("lumina_mare");
        }
        //</editor-fold>
        boolean propozitieNegata = cuvinte.get(0).equals("nu");
        if (propozitieNegata) {
            return new Intentie(new PachetIntentie(App.COMANDA_NEGATA, new String[]{"'nu' la inceputul propozitiei"}));
        }
        List<Integer> comenzi = cautaComenzi(cuvinte);
        if (contineCuv(prop.split(" "), new String[]{"trezeste"})) {
            if (comenzi == null) {
                comenzi = new ArrayList<>();
            }
            System.out.println(comenzi);
            if (!comenzi.contains(App.COMANDA_ALARMA)) {
                comenzi.add(App.COMANDA_ALARMA);
                cuvinte.add("alarma");
            } else {
                cuvinte.add("alarma");
                System.out.println(cuvinte);
            }
        }
        if (comenzi == null) {
            return new Intentie(new PachetIntentie(App.COMANDA_NESUPORTATA, new String[]{"Nu am gasit nicio comanda "
                    + "implementata pana acum din dictionar"}));
        }
        Intentie intentie = new Intentie();
        for (int i = 0; i < comenzi.size(); i++) {
            try {
                String ar[] = cautaComplementi(comenzi.get(i), cuvinte);
                print("comanda primita " + comenzi.get(i) + " cu nr de complementi " + COMPLEMENTI_NECESARI[comenzi.get(i)]
                        + " complementii detectati: " + ar.length + " " + Arrays.toString(ar));
                int cazSpecial = cazSpecial(comenzi.get(i), ar, prop);
                if (complementiIndepliniti(comenzi.get(i), ar.length) || cazSpecial != App.COMANDA_NESUPORTATA) {
                    intentie.add(new PachetIntentie(comenzi.get(i), ar));

//print(comenzi.get(i));
                    if (cazSpecial == App.COMANDA_ALARMA || comenzi.get(i) == App.COMANDA_ALARMA) {
                        insertTimp(prop, intentie.lista.get(intentie.lista.size() - 1));
                    } else if (cazSpecial == App.COMANDA_SETEAZA_INTENSITATE_CULOARE) {
                        insertIntensitateSiCuloare(prop.split(" "), intentie.lista.get(intentie.lista.size() - 1));
                    } else if (cazSpecial == App.COMANDA_MUZICA) {
                        insertMuzica(prop.split(" "), intentie.lista.get(intentie.lista.size() - 1));
                    }
                }
            } catch (Exception ex) {

            }
        }
        //<editor-fold desc="Cazuri speciale si override-uri" defaultstate="collapsed">
        boolean aprindeLumina = false;
        boolean seteazaCuloare = false;
        PachetIntentie tobeRemoved = null;
        for (PachetIntentie p : intentie.lista) {
            if (p.comanda == App.COMANDA_PORNESTE) {
                aprindeLumina = true;
                tobeRemoved = p;
            }
            if (p.comanda == App.COMANDA_SETEAZA_CULOARE) {
                seteazaCuloare = true;
            }

        }
        if (aprindeLumina && seteazaCuloare) {
            intentie.lista.remove(tobeRemoved);
        }
        seteazaCuloare = false;
        boolean seteazaIntensitate = false;
        for (PachetIntentie p : intentie.lista) {
            if (p.comanda == App.COMANDA_SETEAZA_CULOARE) {
                seteazaCuloare = true;
                tobeRemoved = p;
            }
            if (p.comanda == App.COMANDA_SETEAZA_INTENSITATE_CULOARE) {
                seteazaIntensitate = true;
            }

        }
        if (seteazaIntensitate && seteazaCuloare) {
            intentie.lista.remove(tobeRemoved);
        }

        //</editor-fold>
        intentie.finalizare();
        /*int comanda=cautaComanda(cuvinte);
        if (comanda==COMANDA_NESUPORTATA)
            return new int[]{COMANDA_NESUPORTATA};
        if (areComplement(comanda)){
            int complement=cautaComplement(comanda,cuvinte);
            if (complement==COMANDA_AMBIGUA)
                return new int[]{COMANDA_AMBIGUA};

        }else{
            //comanda valida dar fara complement
        }*/
        return intentie;
    }

    public Intentie tradu(String prop) {
        if (LIMBA == App.LIMBA_ENGLEZA) {
            String proposition = prop.toLowerCase();
            Log.println(Log.ASSERT, "TRANSLATOR-EN", "Traducem :" + proposition);
            return translateEnglish(proposition);
        } else {
            String propozitie = faraCaractere_RO(prop.toLowerCase());
            Log.println(Log.ASSERT, "TRANSLATOR-RO", "Traducem :" + propozitie);
            return traducereRomana(propozitie);
        }
    }

    //<editor-fold desc="Functii helper" defaultstate="collapsed">
    private char eCaracterRoman(char c) {
        switch (c) {
            case 'ș':
                return 's';
            case 'ț':
                return 't';
            case 'â':
                return 'a';
            case 'î':
                return 'i';
            case 'ă':
                return 'a';
        }
        return c;
    }

    private String faraCaractere_RO(String sentence) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            char s = eCaracterRoman(sentence.charAt(i));
            b.append(s);
        }
        return b.toString();
    }

    public void setLanguage(int limba) {
        LIMBA = limba;
    }

    static void print(Object x) {
        System.out.println(x);
    }

    static void printAr(int x[]) {
        System.out.println(Arrays.toString(x));
    }

    static void print(Intentie intentie) {
        for (int i = 0; i < intentie.pachet.length; i++) {
            System.out.print(intentie.pachet[i].comanda + " " + Arrays.toString(intentie.pachet[i].detalii) + " |");
        }
        System.out.println();
    }
    //</editor-fold>
/*
    public static void main(String args[]) {
        Traducator traducator = new Traducator();
        traducator.setLanguage(App.LIMBA_ROMANA);
        Intentie x = traducator.tradu("stinge lumina mare");

        // Intentie x=traducator.tradu("porneste luminile intr un minut");
        for (int i = 0; i < x.pachet.length; i++) {
            print("Comanda " + x.pachet[i].comanda);
            for (PachetIntentie pachet : x.pachet) {
                if (pachet.alarma != null) {
                    print("avem un pachet cu alarma de tip " + pachet.alarma.tip + " cu ora "
                            + pachet.alarma.ora + " si minutele " + pachet.alarma.minut);
                }
                if (pachet.comanda == App.COMANDA_SETEAZA_INTENSITATE_CULOARE) {
                    print("avem pachet cu intensitate " + pachet.intensitate + "\nsi culoarea " + pachet.detalii[0]);
                }
                if (pachet.comanda == App.COMANDA_MUZICA) {
                    print("avem pachet de muzica cu piesa " + pachet.piesa.nume);
                }
            }
        }

        // print(Pattern.matches(".* *masina de spalat *.*", "porneste masina de spalat bai cioroiule"));
    }
*/
}
