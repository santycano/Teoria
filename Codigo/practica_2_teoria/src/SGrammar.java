import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SGrammar {
    private Grammar cfg;

    public SGrammar(Grammar cfg) {
        this.cfg = cfg;
    }

    private String why;

    private HashMap<String,HashMap<String, String>> tableTrans;
    private ArrayList<Object[]> stackProcedure;

    public ArrayList<Object[]> getStackProcedure(){
        return stackProcedure;
    }

    public void recognizeSGrammar(String input){
        stackProcedure = new ArrayList<>();
        Stack<String> stack = new Stack<String>();
        stack.push(cfg.getInitialState());
        String topElement;
        String symbol;
        String symbol1;
        String str;
        ArrayList stackvalues = new ArrayList();
        stackvalues.addAll(stack);
        stackProcedure.add(new Object[]{stackvalues,null,"Estado Inicial"});
        for (int i = 0; i < input.length(); i++) {
            stackvalues = new ArrayList();
             symbol = String.valueOf(input.charAt(i));
             if (stack.isEmpty() && symbol.equals("$")){
                System.out.println("Reconocida");
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Acepte"});
                return;
             }
            if (stack.isEmpty()){
                System.out.println("Rechaze");
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Rechaze"});
                return;
            }
            if (!stack.isEmpty() && symbol.equals("$")){
                System.out.println("Reconocida");
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Rechaze"});
                return;
            }
            topElement = stack.peek();

            if (cfg.isTerminal(topElement) && topElement.equals(symbol)){
                stack.pop();
                stackvalues.addAll(stack);
                stackProcedure.add(new Object[]{stackvalues,symbol,"Desapile, Avance"});
            }

            else {
                for (String sy:cfg.getProductionsOriginal().get(topElement)){
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
                }
            }
        }
        System.out.println(stack);

    }

    public void createTableTrans(){
        ArrayList row;
        ArrayList<String> ter = new ArrayList();
        String symbol1;
        String str;
        tableTrans = new HashMap<>();
        short k = 1;
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
                    if (prod.length()==1){
                        if (tableTrans.get(entry.getKey()) == null) tableTrans.put(entry.getKey(),new HashMap<>());
                        tableTrans.get(entry.getKey()).put(symbol,"Desapile, Avance");
                    }
                    else if (symbol.equals(symbol1)){
                        str = new StringBuilder(prod).reverse().toString().substring(0,prod.length()-1);
                        if (tableTrans.get(entry.getKey()) == null) tableTrans.put(entry.getKey(),new HashMap<>());
                        tableTrans.get(entry.getKey()).put(symbol,"Replace "+str+", Avance");
                    }
                }
            }
        }
        for (int i = 0; i < ter.size(); i++) {
            if (tableTrans.get(ter.get(i)) == null) tableTrans.put(ter.get(i),new HashMap<>());
            tableTrans.get(ter.get(i)).put(ter.get(i),"Desapile Avance");
        }
        if (tableTrans.get("∇") == null) tableTrans.put("∇", new HashMap<>());
            tableTrans.get("∇").put("$","Acepte");


        System.out.println(tableTrans);
    }

    public boolean isS(){
        for (Map.Entry<String, ArrayList<String>> entry:cfg.getProductionsOriginal().entrySet()) {
            for (String prod:entry.getValue()) {
                if (!cfg.isTerminal(String.valueOf(prod.charAt(0)))){
                    if (String.valueOf(prod.charAt(0)).equals("&")){
                        why = "Posee una produccion con lambda";
                        return false;
                    }
                    why = "No empieza por un terminal por lo tanto no es S";
                    System.out.println("No es S");
                    return false;
                }
                for (String produ:entry.getValue()) {
                    if (prod.charAt(0)==produ.charAt(0) && !prod.equals(produ)){
                        why = "No empieza por un terminal por lo tanto no es S";
                        System.out.println("No es S1");
                        return false;
                    }
                }
            }
        }
        System.out.println("Es S");
        return true;
    }

    public HashMap<String, HashMap<String, String>> getTableTrans() {
        return tableTrans;
    }

    public String getWhy() {
        return why;
    }
}
