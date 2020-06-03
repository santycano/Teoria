import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class QGrammar {

    private HashMap<String,HashMap<String, String>> tableTrans;
    private Grammar cfg;
    private HashMap<String,ArrayList<Short>> keys;
    public QGrammar(Grammar cfg) {
        this.cfg = cfg;

    }
    private  String why;
    private ArrayList<Object[]> stackProcedure;
    public ArrayList<Object[]> getStackProcedure(){
        return stackProcedure;
    }

    public void recognizeQGrammar(String input){
        stackProcedure = new ArrayList<>();
        Stack<String> stack = new Stack<String>();
        stack.push(cfg.getInitialState());
        String topElement;
        String symbol;
        String symbol1;
        String str;
        ArrayList stackvalues = new ArrayList();
        short k = 0;
        stackvalues.addAll(stack);
        stackProcedure.add(new Object[]{stackvalues,null,"Estado Inicial"});
        for (int i = 0; i < input.length(); i++) {
            stackvalues = new ArrayList();
            System.out.println(stack);
            k = 0;
            symbol = String.valueOf(input.charAt(i));
            if (stack.isEmpty() && symbol.equals("$")){
                System.out.println("Reconocida");
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Acepte"});
                return;
            }


            topElement = stack.peek();
            if (cfg.isTerminal(topElement) && topElement.equals(symbol)){
                stack.pop();
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Desapile, Avance"});
            }
            else {
                if (cfg.isTerminal(topElement)) break;
                for (String sy:cfg.getProductionsOriginal().get(topElement)){
                    if (sy.equals("&")){
                        if ( cfg.getSelectionP().get(keys.get(topElement).get(k)).contains(symbol)){
                            stack.pop();
                            i = i-1;
                            stackvalues.addAll(stack);
                            stackProcedure.add(new Object[]{stackvalues,symbol,"Desapile, Retenga"});
                            break;
                        }
                    }
                    symbol1 = String.valueOf(sy.charAt(0));
                    if (sy.length()==1){
                        stack.pop();
                        stackvalues.addAll(stack);
                        stackProcedure.add(new Object[]{stackvalues,symbol,"Desapile, Avance"});
                        break;
                    }
                    if (symbol.equals(symbol1)){
                        str = new StringBuilder(sy).reverse().toString().substring(0,sy.length()-1);
                        stack.pop();
                        for (int j = 0; j < str.length(); j++) {
                            stack.push(String.valueOf(str.charAt(j)));
                        }
                        stackvalues.addAll(stack);
                        stackProcedure.add(new Object[]{stackvalues,symbol,"Replace "+str+", Avance"});
                        break;
                    }
                    k += 1;
                }
            }
        }
        if (!stack.isEmpty()){
            System.out.println("Reconocida");
            stackvalues.addAll(stack);
            stackProcedure.add(new Object[]{null,null,"Rechaze"});
            return;
        }
        System.out.println(stack);
    }

    public void createTableTrans(){
        ArrayList row;
        ArrayList<String> ter = new ArrayList();
        String symbol1;
        String str;
        tableTrans = new HashMap<>();
        short k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:cfg.getProductionsOriginal().entrySet()) {
            for (String prod:entry.getValue()) {
                for (int i = 1; i < prod.length(); i++) {
                    if (cfg.isTerminal(String.valueOf(prod.charAt(i)))){
                        if (!ter.contains(String.valueOf(prod.charAt(i)))) ter.add(String.valueOf(prod.charAt(i)));
                    }
                }
            }
        }
        for (Map.Entry<String, ArrayList<String>> entry:cfg.getProductionsOriginal().entrySet()) {
            for (String prod:entry.getValue()) {
                symbol1 = String.valueOf(prod.charAt(0));
                for (String symbol:cfg.getTerminals()) {
                    if (prod.length()==1 && !prod.equals("&")){
                        if (tableTrans.get(entry.getKey()) == null) tableTrans.put(entry.getKey(),new HashMap<>());
                        tableTrans.get(entry.getKey()).put(symbol,"Desapile, Avance");
                    }
                    else if (symbol.equals(symbol1)){
                        str = new StringBuilder(prod).reverse().toString().substring(0,prod.length()-1);
                        if (tableTrans.get(entry.getKey()) == null) tableTrans.put(entry.getKey(),new HashMap<>());
                        tableTrans.get(entry.getKey()).put(symbol,"Replace "+str+", Avance");
                    }
                    else if ( cfg.getSelectionP().get(k).contains(symbol)){
                        if (tableTrans.get(entry.getKey()) == null) tableTrans.put(entry.getKey(),new HashMap<>());
                        tableTrans.get(entry.getKey()).put(symbol,"Desapile, Retenga");
                    }
                }
                if (prod.equals("&")){
                    if (cfg.getSelectionP().get(k).contains("$")) tableTrans.get(entry.getKey()).put( "$","Desapile, Retenga");
                }
                k += 1;
            }
        }
        for (int i = 0; i < ter.size(); i++) {
            if (tableTrans.get(ter.get(i)) == null) tableTrans.put(ter.get(i),new HashMap<>());
            tableTrans.get(ter.get(i)).put(ter.get(i),"Desapile, Avance");
        }
        if (tableTrans.get("∇") == null) tableTrans.put("∇", new HashMap<>());
        tableTrans.get("∇").put("$","Acepte");


        System.out.println(tableTrans);
    }

    public boolean isQ(){
        short k = 0;
        keys = new HashMap();
        for (Map.Entry<String, ArrayList<String>> entry:cfg.getProductionsOriginal().entrySet()) {
            for (String prod : entry.getValue()){
                if (keys.get(entry.getKey()) == null){
                    keys.put(entry.getKey(),new ArrayList<>());
                    keys.get((entry.getKey())).add(k);
                }
                else keys.get((entry.getKey())).add(k);
                k += 1;
            }
        }
        k = 0;
        for (Map.Entry<String, ArrayList<String>> entry:cfg.getProductionsOriginal().entrySet()) {
            for (String prod:entry.getValue()) {
                if (!cfg.isTerminal(String.valueOf(prod.charAt(0)))){

                    if (!prod.equals("&")) {
                        why = "No empieza por un terminal o lambda por lo tanto no es Q";
                        System.out.println("No es Q");
                        return false;
                    }
                }
                if (prod.equals("&")){
                    for (Short num : keys.get(entry.getKey())) {
                        for (Short num1 : keys.get(entry.getKey())) {
                            if (num != num1){
                                if (areListsSimilar(cfg.getSelectionP().get(num),cfg.getSelectionP().get(num1))){
                                    why = "Los conjuntos de seleccion de "+(num+1)+" y "+(num1+1)+" no son disyuntos por lo tanto no es Q";
                                    System.out.println("No es Q");
                                    return false;
                                }
                            }
                        }
                    }
                }
                k +=1;
            }
        }
        System.out.println("Es Q");
        return true;
    }

    public HashMap<String, HashMap<String, String>> getTableTrans() {
        return tableTrans;
    }

    private static boolean areListsSimilar(ArrayList<String> list1, ArrayList<String> list2) {
        for (String item1 : list1) {
            for (String item2 : list2) {
                if (item1.equals(item2)) return true;
            }
        }
        return false;
    }

    public String getWhy() {
        return why;
    }
}
