import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Procedure {
    ArrayList<Double> vars;
    ArrayList<String> keys;
    ArrayList<String> src;

    public Procedure(String name) throws FileNotFoundException {
        src = new ArrayList<>();
        vars = new ArrayList<>();
        keys = new ArrayList<>();
        File srcFile = new File("./examples/" + name + ".ipl");
        Scanner reader = new Scanner(srcFile);
        while (reader.hasNextLine()) {
            String data = StringUtils.removeSpaces(reader.nextLine());
            if (data.length() > 0)
                src.add(data);
        }
        reader.close();
    }

    public String toString() {
        return "Procedure{" +
                "vars=" + vars +
                ", keys=" + keys +
                ", src=" + src +
                '}';
    }

    public double doMath(String math) {
        String newMath = StringUtils.removeSpaces(math);
        for (int i = 0; i < vars.size(); i++)
            newMath = newMath.replaceAll(keys.get(i), Double.toString(vars.get(i)));
        try{
            return Double.parseDouble(math);
        }catch (Exception e){}
        return MathParser.parseMath(newMath);
    }

    public double runReturn(String line) {
        String ref = line.substring(6);
        for (int i = 0; i < keys.size(); i++) {
            if (ref.equals(keys.get(i)))
                return vars.get(i);
        }
        return doMath(ref);
    }

    public void runParam(String line, double value) {
        String name = line.substring(5);
        keys.add(name);
        vars.add(value);
    }

    public void runVar(String line){
        int equalsIdx = line.indexOf("=");
        String name = line.substring(3,equalsIdx);
        String ref = line.substring(equalsIdx+1);
        if (keys.contains(name))
            System.out.println("Variable already declared");
        else{
            keys.add(name);
            vars.add(doMath(ref));
        }
    }

    public boolean runIf(String line){
        String ref = line.substring(2);
        for (int i = 0; i < vars.size(); i++)
            ref = ref.replaceAll(keys.get(i), Double.toString(vars.get(i)));
        return BooleanParser.parseBool(ref);
    }

    public void runRes(String line) throws FileNotFoundException {
        for (int i = 0; i < vars.size(); i++)
            line = line.replaceAll(keys.get(i), Double.toString(vars.get(i)));
        int equalsIdx = line.indexOf("=");
        int colonIdx = line.indexOf(":");
        String varName = line.substring(3,equalsIdx);
        String procName = line.substring(equalsIdx+1,colonIdx);
        String[] strungParams = line.substring(colonIdx+1).split(",");
        double[] newParams = new double[strungParams.length];
        int idx = 0;
        for (String s:strungParams){
            newParams[idx] = doMath(s);
            idx++;
        }
        Procedure newProc = new Procedure(procName);
        keys.add(varName);
        vars.add(newProc.run(newParams));
    }

    public double run(double[] params) throws FileNotFoundException {
        int paramNum = 0;
        boolean runnable = true;
        for (String line : src) {
            if (line.equals("over")) {
                runnable = true;
                continue;
            }
            if (runnable) {
                if (line.startsWith("return")) {
                    return runReturn(line);
                } else if (line.startsWith("param")) {
                    runParam(line, params[paramNum]);
                    paramNum++;
                } else if (line.startsWith("var")) {
                    runVar(line);
                } else if (line.startsWith("res")) {
                    runRes(line);
                } else if (line.startsWith("if")) {
                    if (!runIf(line))
                        runnable = false;
                }
            }
        }
        return 0;
    }

    public static void main(String[] args) throws FileNotFoundException {
        double[] params = new double[args.length - 1];
        int argNum = 0;
        String procName = "";
        for (String arg : args) {
            if (argNum == 0)
                procName = arg;
            else
                params[argNum - 1] = Double.parseDouble(arg);
            argNum++;
        }
        Procedure proc = new Procedure(procName);
        proc.run(params);
        System.out.println(proc.run(params));
    }
}
