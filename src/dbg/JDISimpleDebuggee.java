package dbg;

public class JDISimpleDebuggee {
    String jesuispastemporaire = "Je suis pas temporaire";

    public static void main(String[] args) {
        String description = "Simple power printer";
        System.out.println(description + " -- starting");
        int x = 40;
        int power = 2;
        printPower(x, power);
    }

    public static double power(int x, int power) {
        double powerX = Math.pow(x, power);
        StringBuilder salutGRosBGT = new StringBuilder("Salut");

        for (int i = 0; i < 10; i++) {
            salutGRosBGT.append("Salut");
            int toto = 4;
        }
        return powerX;
    }

    public static void printPower(int x, int power) {
        double powerX = power(x, power);
        System.out.println(powerX);
    }
}