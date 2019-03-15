import java.io.*;
import java.lang.*;
import java.sql.*;
import java.util.*;

public class Main {

  public static void main(String[] args) {
    try {
      Database db = new Database();
      GUI gui = new GUI();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}