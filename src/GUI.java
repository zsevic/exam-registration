import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import javax.swing.*;

public class GUI {
  public JFrame mainFrame;
  public JLabel headerLabel;
  public JLabel statusLabel;
  public JTextArea examsArea;
  public JTextArea ordinalNumberArea;
  public JPanel indexPanel;
  public JPanel numberPanel;
  public JPanel examsPanel;
  public JLabel registrationLabel;

  public GUI() {
    mainFrame = new JFrame("Prijavljivanje ispita");
    mainFrame.setSize(600, 1000);
    mainFrame.setLayout(new GridLayout(5, 1));

    mainFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        try {
          Database.con.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        System.exit(0);
      }

    });

    headerLabel = new JLabel("", JLabel.CENTER);
    headerLabel.setText("Prijava ispita za ispitni rok jun 2015");

    statusLabel = new JLabel("", JLabel.CENTER);

    indexPanel = new JPanel();
    indexPanel.setLayout(new FlowLayout());

    numberPanel = new JPanel();
    numberPanel.setLayout(new FlowLayout());

    examsPanel = new JPanel();
    examsPanel.setLayout(new GridLayout(2, 1));

    mainFrame.add(headerLabel);
    mainFrame.add(indexPanel);
    mainFrame.add(statusLabel);
    mainFrame.setVisible(true);

    showIndex();
  }

  public void showIndex() {
    JLabel indexLabel = new JLabel("Indeks: ", JLabel.CENTER);
    final JTextField indexText = new JTextField(6);

    JButton confirmButton = new JButton("Potvrdi");
    confirmButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        try {
          final int indeks = Integer.parseInt(indexText.getText());
          System.out.println("res:" + indeks);
          int res = Database.registeredYears(indeks);

          if (res == 0) {
            statusLabel.setText("Indeks nije pronadjen u bazi!");
          } else if (res < 3) {
            statusLabel.setText("Student je upisao manje od tri godine");
          } else {
            String exams = Database.unpassedExams(indeks);

            if (exams == null) {
              statusLabel.setText("greska!");
              examsPanel.removeAll();
              return;
            }

            String unpassedExams = Database.unpassedExams(indeks);
            statusLabel.setText(unpassedExams);
            showExams(indeks);
          }

        } catch (NumberFormatException e1) {
          statusLabel.setText("Unesite pravilno!");
          examsPanel.removeAll();
          e1.printStackTrace();
        }
      }
    });

    indexPanel.add(indexLabel);
    indexPanel.add(indexText);
    indexPanel.add(confirmButton);

    mainFrame.setVisible(true);
  }

  public void showExams(final int indeks) {
    System.out.println(indeks);

    JLabel ordinalNumberLabel = new JLabel("Redni broj ispita: ", JLabel.CENTER);
    final JTextField ordinalNumberText = new JTextField(6);
    JButton registerButton = new JButton("Dodaj");
    registrationLabel = new JLabel("", JLabel.CENTER);
    registerButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        try {
          int i = Integer.parseInt(ordinalNumberText.getText());
          int res = Database.registerExam(indeks, Database.examsLeft.get(i - 1));

          if (res != -1) {
            String unpassedExams = Database.unpassedExams(indeks);
            statusLabel.setText(unpassedExams);
            registrationLabel.setText("Ispit je uspesno dodat!");

          } else {
            registrationLabel.setText("Ispit nije dodat!");
          }

        } catch (Exception e1) {
          registrationLabel.setText("Unesi pravilno redni broj!");

        }
      }
    });

    numberPanel.removeAll();
    numberPanel.add(ordinalNumberLabel);
    numberPanel.add(ordinalNumberText);
    numberPanel.add(registerButton);
    numberPanel.add(registrationLabel);

    examsPanel.removeAll();
    examsPanel.add(numberPanel);
    examsPanel.add(registrationLabel);

    mainFrame.add(examsPanel);
    mainFrame.setVisible(true);
  }

}