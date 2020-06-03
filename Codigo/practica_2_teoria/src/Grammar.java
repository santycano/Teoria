import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class Grammar {
    private ArrayList<String> nonTerminals;
    private ArrayList<String> nonTerminalsNull;
    private ArrayList<String> terminals;
    private ArrayList<Short> productionsNull;
    private HashMap<String, ArrayList<String>> firstN;
    private HashMap<Short, ArrayList<String>> firstP;
    private HashMap<Short, ArrayList<String>> selectionP;
    private HashMap<String, ArrayList<String>> follow;
    private HashMap<String, ArrayList<String>> productions;
    private HashMap<String, ArrayList<String>> productionsOriginal;
    //private HashMap<String, ArrayList<String, String>> mTableFirstRule = null;
    private String initialState = "";

    public Grammar(){
        this.productions = new HashMap<>();
        this.productionsOriginal = new HashMap<>();
        this.terminals = new ArrayList();
        this.nonTerminals = new ArrayList<>();
        this.productionsNull = new ArrayList<Short>();
        this.firstN = new HashMap<>();
        this.follow = new HashMap<>();
        this.firstP = new HashMap<>();
        this.selectionP = new HashMap<>();
        this.nonTerminalsNull = new ArrayList<>();
        //this.mTableFirstRule = new HashMap<>();
    }

   // private boolean isNonTerminal(char symbol){
    //    return symbol >= 'A' && symbol <= 'Z';
    //}

    public boolean isNonTerminal(String symbol){
        return nonTerminals.contains(symbol);
    }

    public boolean isTerminal(String symbol){
        return terminals.contains(symbol);
    }

    public boolean isnonTerminalsNull(String symbol){
        return nonTerminalsNull.contains(symbol);
    }

    public boolean isproductionNull(short symbol){
        return productionsNull.contains(symbol);
    }

    public void init(ArrayList<String> grammarEquations){
        for(String grammarEq : grammarEquations){
            String splittedGE[] = grammarEq.split("->");
            String nonTerminal = splittedGE[0];
            String production = splittedGE[1];
            if(initialState.equals("")) initialState = nonTerminal;
            if(productions.get(nonTerminal) == null){
                productions.put(nonTerminal, new ArrayList<>());
                productionsOriginal.put(nonTerminal, new ArrayList<>());
                nonTerminals.add(nonTerminal);
            }
            productions.get(nonTerminal).add(production);
            productionsOriginal.get(nonTerminal).add(production);
        }
        short k = 0;
        for(Map.Entry<String, ArrayList<String>> entry : productions.entrySet()) {
            String nonTerminal = entry.getKey();
            if(firstN.get(nonTerminal) == null){
                firstN.put(nonTerminal, new ArrayList<>());
                follow.put(nonTerminal, new ArrayList<>());
            }
            for(String production : entry.getValue()){
                findT(production);
            }
        }
        for (Map.Entry<String, ArrayList<String>> entry:productionsOriginal.entrySet()) {
            for (String prod:entry.getValue()) {
                firstP.put(k,new ArrayList<>());
                selectionP.put(k,new ArrayList<>());
                k += 1;
            }
        }
        findNullN();
        findFirstN();
        findFollowN();
        findFirstP();
        findProductionNull();
        findSelectionP();
        nonTerminals = removeDuplicates(nonTerminals);
        terminals = removeDuplicates(terminals);
        nonTerminalsNull = removeDuplicates(nonTerminalsNull);
        productionsNull = removeDuplicates(productionsNull);
        System.out.println(nonTerminals);
        System.out.println(terminals);
        System.out.println(nonTerminalsNull);
        System.out.println(firstN);
        System.out.println(follow);
        System.out.println(firstP);
        System.out.println(productionsNull);
        System.out.println(removeDuplicate(selectionP));
    }

    private void findT(String production){
        for(int i = 0; i < production.length(); i++){
            String symbolC = String.valueOf(production.charAt(i));
            if (!isNonTerminal(symbolC) && !symbolC.equals("&") && !terminals.contains(symbolC)){
                terminals.add(symbolC);
            }
        }
    }

    private void findNullN(){
        short k = 0;
        find$(k);
    }

    private void find$(short k){
        k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:productions.entrySet()) {
            for (String prod : entry.getValue()) {
                if (prod.matches("[&]*") && !nonTerminalsNull.contains(entry.getKey())) {
                        nonTerminalsNull.add(entry.getKey());
                        k = 1;
                        break;
                }
            }
        }
        if (k == 1) {
            replaceLambda();
            find$(k);
        }
    }

    private void replaceLambda(){
        for (Map.Entry<String, ArrayList<String>> entry:productions.entrySet()) {
            for (String prod:entry.getValue()) {
                for(int i = 0; i < prod.length(); i++){
                    String symbolC = String.valueOf(prod.charAt(i));
                    if (nonTerminalsNull.contains(symbolC)){
                        productions.get(entry.getKey()).set(productionsOriginal.get(entry.getKey()).indexOf(prod),productions.get(entry.getKey()).get(productionsOriginal.get(entry.getKey()).indexOf(prod)).replaceAll(symbolC,"&"));
                    }
                }
            }
        }
    }

    private void findFirstN(){
        short k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:productionsOriginal.entrySet()) {
            k = 0;
            for (String prod:entry.getValue()) {
                for(int i = 0; i < prod.length(); i++){
                    String symbolC = String.valueOf(prod.charAt(i));
                    if (isTerminal(symbolC)){
                        firstN.get(entry.getKey()).add(symbolC);
                        k = 1;
                        break;
                    }
                    if (isNonTerminal(symbolC) && !isnonTerminalsNull(symbolC)){
                        firstN.get(entry.getKey()).add("first/"+symbolC);
                        k = 1;
                        break;
                    }
                    if (isNonTerminal(symbolC) && isnonTerminalsNull(symbolC)){
                        firstN.get(entry.getKey()).add("first/"+symbolC);
                    }
                }
            }
        }
        String prod = null;
        int size = 0;
        do {
            k = 0;
            for (Map.Entry<String, ArrayList<String>> entry:firstN.entrySet()) {
                size = entry.getValue().size();
                for (int i = 0; i < size; i++) {
                    prod = entry.getValue().get(i);
                    if (prod.split("/")[0].equals("first")){
                        k = 1;
                        for (String firs : firstN.get(prod.split("/")[1])) {
                            firstN.get(entry.getKey()).add(firs);
                        }
                        firstN.get(entry.getKey()).remove(firstN.get(entry.getKey()).indexOf(prod));
                    }
                }
            }
        }while (k==1);

    }

    private void findFollowN(){
        String symbolC;
        String next;
        short j;
        short k = 0;
        follow.get(initialState).add("$");
        for (Map.Entry<String, ArrayList<String>> entry:productionsOriginal.entrySet()) {
            for (String prod:entry.getValue()) {
                for(short i = 0; i < prod.length(); i++){
                    symbolC = String.valueOf(prod.charAt(i));
                    if (symbolC.equals("&")) break;
                    if (isNonTerminal(symbolC) && i == prod.length()-1){
                        follow.get(symbolC).add("follow/"+entry.getKey());
                        break;
                    }
                    if (i == prod.length()-1) break;
                    next = String.valueOf(prod.charAt(i+1));
                    if (isNonTerminal(symbolC) && isTerminal(next)){
                        follow.get(symbolC).add(next);
                    }
                    if (isNonTerminal(symbolC) && isNonTerminal(next) && !isnonTerminalsNull(next)){
                        for (String fir : firstN.get(next)) {
                            follow.get(symbolC).add(fir);
                        }
                    }
                    outer:
                    if (isNonTerminal(symbolC) && isNonTerminal(next) && isnonTerminalsNull(next)){
                        j = (short) (i+1);
                        while (isnonTerminalsNull(String.valueOf(prod.charAt(j)))){
                            for (String fir : firstN.get(String.valueOf(prod.charAt(j)))) {
                                follow.get(symbolC).add(fir);
                            }
                            if (j == prod.length()-1){
                                follow.get(symbolC).add("follow/"+entry.getKey());
                                break outer;
                            }
                            j += 1;
                        }
                        if (isNonTerminal(String.valueOf(prod.charAt(j))) && isTerminal(String.valueOf(prod.charAt(j+1)))){
                            follow.get(symbolC).add(String.valueOf(prod.charAt(j+1)));
                        }
                        if (isNonTerminal(String.valueOf(prod.charAt(j))) && isNonTerminal(String.valueOf(prod.charAt(j+1)))){
                            for (String fir : firstN.get(String.valueOf(prod.charAt(j+1)))) {
                                follow.get(symbolC).add(fir);
                            }
                        }
                    }
                }
            }
        }
        replaceFollow();

    }

    private void replaceFollow(){
        String prod = null;
        short k;
        int size = 0;

        removeFollowSameKey(size,prod);

        for (Map.Entry<String, ArrayList<String>> entry:follow.entrySet()) {
            size = entry.getValue().size();
            for (int i = 0; i < size; i++) {
                k = 0;
                prod = entry.getValue().get(i);
                if (prod.split("/")[0].equals("follow") && k == 0){
                    follow.get(entry.getKey()).addAll(follow.get(prod.split("/")[1]));
                    follow.get(entry.getKey()).remove(follow.get(entry.getKey()).indexOf(prod));
                    size = size - 1;
                }
            }
        }
        ArrayList<String> keys = new ArrayList();
        for (Map.Entry<String, ArrayList<String>> entry:follow.entrySet()) {
            //entry.setValue(removeDuplicates(entry.getValue()));
            keys.add(entry.getKey());
        }

        for (int i = 0; i < keys.size(); i++) {
            follow.replace(keys.get(i),removeDuplicates(follow.get(keys.get(i))));
        }

        for (int i = 0; i < keys.size(); i++) {
            for (int j = 0; j < follow.get(keys.get(i)).size(); j++) {
                if (follow.get(keys.get(i)).get(j).contains("follow")){
                    replaceFollow();
                }
            }
        }
    }

    private void removeFollowSameKey(int size,String prod){
        for (Map.Entry<String, ArrayList<String>> entry:follow.entrySet()) {
            size = entry.getValue().size();
            for (int i = 0; i < size; i++) {
                prod = entry.getValue().get(i);
                if (prod.split("/")[0].equals("follow") && prod.split("/")[1].equals(entry.getKey())){
                    follow.get(entry.getKey()).remove(follow.get(entry.getKey()).indexOf(prod));
                    size = size - 1;
                }
            }
        }
    }

    private HashMap<Short, ArrayList<String>> removeDuplicate(HashMap<Short, ArrayList<String>> list){
        for (Map.Entry<Short, ArrayList<String>> entry:list.entrySet()) {
            ArrayList arrayList = removeDuplicates(entry.getValue());
            list.get(entry.getKey()).clear();
            list.get(entry.getKey()).addAll(arrayList);
        }
        return list;
    }

    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

    private void findFirstP(){
        short k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:productionsOriginal.entrySet()) {
            for (String prod:entry.getValue()) {
                for(int i = 0; i < prod.length(); i++){
                    String symbolC = String.valueOf(prod.charAt(i));
                    if (isTerminal(symbolC)){
                        firstP.get(k).add(symbolC);
                        break;
                    }
                    if (isNonTerminal(symbolC) && !isnonTerminalsNull(symbolC)){
                        firstP.get(k).add("first/"+symbolC);
                        break;
                    }
                    if (isNonTerminal(symbolC) && isnonTerminalsNull(symbolC)){
                        firstP.get(k).add("first/"+symbolC);
                    }
                }
                k += 1;
            }

        }
        String prod = null;
        int size = 0;
        do {
            k = 0;
            for (Map.Entry<Short, ArrayList<String>> entry:firstP.entrySet()) {
                size = entry.getValue().size();
                for (int i = 0; i < size; i++) {
                    prod = entry.getValue().get(i);
                    if (prod.split("/")[0].equals("first")){
                        k = 1;
                        for (String firs : firstN.get(prod.split("/")[1])) {
                            firstP.get(entry.getKey()).add(firs);
                        }
                        firstP.get(entry.getKey()).remove(firstP.get(entry.getKey()).indexOf(prod));
                    }
                }
            }
        }while (k==1);
    }

    private void findProductionNull(){
        short k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:productions.entrySet()) {
            for (String prod:entry.getValue()) {
                for(int i = 0; i < prod.length(); i++) {
                    if (prod.matches("[&]*") && !productionsNull.contains(entry.getKey())) {
                        productionsNull.add(k);
                        break;
                    }
                }
                k += 1;
            }
        }
    }

    private void findSelectionP(){
        short k = 0;
        short d;
        for (Map.Entry<String, ArrayList<String>> entry:productionsOriginal.entrySet()) {
            for (String prod:entry.getValue()) {
                outer:
                for(int i = 0; i < prod.length(); i++){
                    String symbolC = String.valueOf(prod.charAt(i));
                    if (isTerminal(symbolC)){
                        selectionP.get(k).add(symbolC);
                        break;
                    }
                    if (isproductionNull(k) ){
                        selectionP.get(k).add("follow/"+entry.getKey());
                    }
                    if (isNonTerminal(symbolC) && !isnonTerminalsNull(symbolC)){
                        selectionP.get(k).add("first/"+symbolC);
                        break;
                    }
                    if (isNonTerminal(symbolC) && isnonTerminalsNull(symbolC)){
                        d = (short) (i);
                        while (isnonTerminalsNull(String.valueOf(prod.charAt(d)))){
                            selectionP.get(k).add("first/"+String.valueOf(prod.charAt(d)));
                            if (d == prod.length()-1){
                                break outer;
                            }
                            d += 1;
                        }
                        if (isTerminal(String.valueOf(prod.charAt(d)))){
                            selectionP.get(k).add(String.valueOf(prod.charAt(d)));
                            break;
                        }
                        break;
                    }
                }
                k += 1;
            }
        }

        String prod = null;
        int size = 0;
        for (Map.Entry<Short, ArrayList<String>> entry:selectionP.entrySet()) {
            size = entry.getValue().size();
            for (int i = 0; i < size; i++) {
                prod = entry.getValue().get(i);
                if (prod.split("/")[0].equals("first")){
                    for (String firs : firstN.get(prod.split("/")[1])) {
                        selectionP.get(entry.getKey()).add(firs);
                    }
                    selectionP.get(entry.getKey()).remove(selectionP.get(entry.getKey()).indexOf(prod));
                }
            }
        }

        for (Map.Entry<Short, ArrayList<String>> entry:selectionP.entrySet()) {
            for (int j = 0; j < entry.getValue().size(); j++) {
                prod = entry.getValue().get(j);
                if (prod.split("/")[0].equals("follow")){
                    selectionP.get(entry.getKey()).remove(selectionP.get(entry.getKey()).indexOf(prod));
                    selectionP.get(entry.getKey()).addAll(follow.get(prod.split("/")[1]));
                }
            }
        }
    }

    public ArrayList<String> getNonTerminals() {
        return nonTerminals;
    }

    public ArrayList<String> getNonTerminalsNull() {
        return nonTerminalsNull;
    }

    public ArrayList<String> getTerminals() {
        return terminals;
    }

    public HashMap<String, ArrayList<String>> getProductionsOriginal() {
        return productionsOriginal;
    }

    public String getInitialState() {
        return initialState;
    }

    public HashMap<String, ArrayList<String>> getFirstN() {
        return firstN;
    }

    public HashMap<Short, ArrayList<String>> getFirstP() {
        return firstP;
    }

    public HashMap<Short, ArrayList<String>> getSelectionP() {
        return selectionP;
    }

    public HashMap<String, ArrayList<String>> getFollow() {
        return follow;
    }
}