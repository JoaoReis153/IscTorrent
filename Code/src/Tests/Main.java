package Tests;

import GUI.GUI;

public class Main {
    public static void main(String[] args) {
        GUI window = new GUI("192.168.1.1", 80);
        window.open();
    }
    
}
 