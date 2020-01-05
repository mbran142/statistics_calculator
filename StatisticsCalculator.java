import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.StringTokenizer;

class StatisticsDisplay extends JPanel implements ActionListener {

    private JTextField sequence, amount, sample, specific;
    private JCheckBox less, equal, greater, print, consecutive;
    private JTextArea console;

    private final int SPEC_SIZE = 8;
    private final String mainCard = "main";
    private final String manualCard = "manual";

    static {
        System.loadLibrary("stats");
    }

    private final Color errColor = new Color(0xE26263);

    public StatisticsDisplay(String initialSequence) {

        this.setLayout(new CardLayout());
        GridBagConstraints spec;

        //prepare sequence field
        sequence = new JTextField(initialSequence);
        sequence.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        sequence.setBackground(Color.WHITE);
        sequence.setPreferredSize(new Dimension(350,30));
        sequence.setFont(new Font(sequence.getFont().getName(), Font.BOLD, 16));
        JPanel seqPanel = new JPanel(new BorderLayout());
        seqPanel.add(sequence);

        //specification panel fields
        amount = new JTextField();
        amount.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        amount.setPreferredSize(new Dimension(150,20));
        amount.setBackground(Color.WHITE);
        sample = new JTextField();
        sample.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        sample.setPreferredSize(new Dimension(150,20));
        sample.setBackground(Color.WHITE);
        specific = new JTextField();
        specific.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        specific.setPreferredSize(new Dimension(150,20));
        specific.setBackground(Color.WHITE);
        less = new JCheckBox("<");
        equal = new JCheckBox("=");
        equal.setSelected(true);
        greater = new JCheckBox(">");
        greater.setSelected(true);
        JPanel equalityPanel = new JPanel(new GridLayout(1,3,30,0));
        equalityPanel.setOpaque(false);
        equalityPanel.add(less);
        equalityPanel.add(equal);
        equalityPanel.add(greater);
        print = new JCheckBox("Print");
        consecutive = new JCheckBox("Consecutive");

        //prepare specification panel
        JPanel specificationPanel = new JPanel(new GridBagLayout());
        specificationPanel.setBackground(new Color(220,220,205));
        specificationPanel.setBorder(BorderFactory.createEmptyBorder(20,30,20,30));
        spec = new GridBagConstraints();
        spec.insets = new Insets(5,5,5,40);
        spec.anchor = GridBagConstraints.WEST;
        spec.gridx = 0;
        spec.gridy = 0;
        specificationPanel.add(new JLabel("<html><font size='4' color='#000000'> Amount </font></html>"), spec);
        spec.gridy++;
        specificationPanel.add(new JLabel("<html><font size='4' color'#000000'> Sample </font></html>"), spec);
        spec.gridy++;
        specificationPanel.add(new JLabel("<html><font size='4' color'#000000'> Specific </font></html>"), spec);
        spec.gridy++;
        specificationPanel.add(new JLabel("<html><font size='4' color='#000000'> Equality </font></html>"), spec);
        spec.gridy++;
        spec.anchor = GridBagConstraints.EAST;
        specificationPanel.add(print, spec);

        spec.insets = new Insets(5,40,5,5);
        spec.gridx++;
        spec.gridy = 0;
        specificationPanel.add(amount, spec);
        spec.gridy++;
        specificationPanel.add(sample, spec);
        spec.gridy++;
        specificationPanel.add(specific, spec);
        spec.gridy++;
        specificationPanel.add(equalityPanel, spec);
        spec.gridy++;
        spec.anchor = GridBagConstraints.CENTER;
        specificationPanel.add(consecutive, spec);

        //prepare left side panel
        JPanel leftPanel = new JPanel(new GridBagLayout());
        spec = new GridBagConstraints();
        spec.anchor = GridBagConstraints.WEST;
        spec.gridy = 0;
        leftPanel.add(new JLabel("<html><font size='6' color='#000000'> Sequence </font></html>"), spec);
        spec.gridy++;
        leftPanel.add(seqPanel, spec);
        spec.gridy++;
        leftPanel.add(Box.createRigidArea(new Dimension(0,40)), spec);
        spec.gridy++;
        leftPanel.add(new JLabel("<html><font size='6' color='#000000'> Specifications </font></html>"), spec);
        spec.gridy++;
        leftPanel.add(specificationPanel, spec);
        spec.gridy++;
        leftPanel.add(Box.createRigidArea(new Dimension(0,30)), spec);
        spec.gridy++;
        JButton startButton = new JButton("Start");
        startButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        startButton.setForeground(Color.BLACK);
        startButton.setPreferredSize(new Dimension(100,40));
        startButton.setBackground(new Color(150,100,120));
        startButton.addActionListener(this);
        spec.anchor = GridBagConstraints.CENTER;
        leftPanel.add(startButton, spec);
        leftPanel.setOpaque(false);

        //prepare console window
        console = new JTextArea();
        console.setBackground(new Color(0x101010));
        console.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        console.setForeground(new Color(0xffffff));
        console.setFont(new Font("Monospaced", Font.PLAIN, 11));
        console.setEditable(false);

        //button to clear console
        JButton clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(150,100,120));
        clearButton.setForeground(new Color(0x0));
        clearButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        clearButton.setPreferredSize(new Dimension(40,30));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                console.setText("");
            }
        });

        //prepare right side panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(new JLabel("<html><font size='6' color='#000000'> Output </font></html>"), BorderLayout.NORTH);
        JScrollPane consolePane = new JScrollPane(console);
        consolePane.getViewport().setPreferredSize(new Dimension(400,500));
        rightPanel.add(consolePane, BorderLayout.CENTER);
        rightPanel.add(clearButton, BorderLayout.SOUTH);
        rightPanel.setOpaque(false);

        //finalize main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1,2,40,0));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        mainPanel.setBackground(new Color(240,240,225));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        this.add(mainPanel, mainCard);
        this.add(this.createManualPanel(), manualCard);
    }

    private JPanel createManualPanel() {

        JPanel output = new JPanel();
        output.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        output.setBackground(new Color(190, 190, 175));
        output.setLayout(new BorderLayout());
        output.add(new JLabel("<html><font size='8' color='#000000'> User Manual </font></html>"), BorderLayout.NORTH);
        JButton backButton = new JButton("Back");
        backButton.setBackground(new Color(150,100,120));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainPanel();
            }
        });
        JPanel backButtonPanel = new JPanel(new GridLayout(1,3));
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(Box.createRigidArea(new Dimension(500,0)));
        backButtonPanel.add(backButton);
        backButtonPanel.add(Box.createRigidArea(new Dimension(500,0)));
        backButton.setPreferredSize(new Dimension(300,40));
        output.add(backButtonPanel, BorderLayout.SOUTH);
        output.add(new JLabel(
                "<html><font color='#000000' size='5'>" +
                        "<u>Amount Field</u>: The amount of numbers required for a check. Must be filled.<br><br>" +
                        "<u>Sample Field</u>: The amount of numbers checked. Default is sequence size.<br><br>" +
                        "<u>Specific Field</u>: Checks for a specific number. Defaults to no specific.<br><br>" +
                        "<u>Equality Checkboxes</u>: Specify less than, equal to, or greater than the amount.<br><br>" +
                        "<u>Print Checkbox</u>: Print out each iteration of the sequence.<br><br>" +
                        "<u>Consecutive Checkbox</u>: Only checks a sequence if numbers are in a row.<br><br>" +
                        "</font></html>"
        ));

        return output;
    }

    private int[] validSimulation() {

        //if simulation has proper syntax (3 2[3] 1x5 => 3, 2, 2, 2, 1, 1, 1, 1, 1)
        String tempStr;
        int[] sequenceArr;
        int i, temp, arraySize = 0;
        StringTokenizer st = new StringTokenizer(sequence.getText(), " ");

        if (sequence.getText().contains("-")) {
            sequence.setBackground(errColor);
            return null;
        }

        //check array size and legitimacy
        while (st.hasMoreTokens()) {

            tempStr = st.nextToken();
            i = 0;
            while (i < tempStr.length() && tempStr.charAt(i) >= '0' && tempStr.charAt(i) <= '9')
                i++;

            if (i < tempStr.length()) try {
                switch (tempStr.charAt(i)) {
                    case '[': arraySize += Integer.parseInt(tempStr.substring(i + 1, tempStr.length() - 1)); break;
                    case 'x': arraySize += Integer.parseInt(tempStr.substring(i + 1)); break;
                    default : sequence.setBackground(errColor); return null;
                }
            } catch (NumberFormatException e) {
                sequence.setBackground(errColor);
                return null;
            }
            else arraySize++;
        }

        sequenceArr = new int[arraySize + SPEC_SIZE];
        st = new StringTokenizer(sequence.getText(), " ");
        int j = SPEC_SIZE, amnt;

        //populate array
        while (st.hasMoreTokens()) {

            tempStr = st.nextToken();
            amnt = temp = i = 0;
            while (i < tempStr.length() && tempStr.charAt(i) >= '0' && tempStr.charAt(i) <= '9') {
                temp *= 10;
                temp += tempStr.charAt(i++) & 0xf;
                if (temp > 10000000) {
                    sequence.setBackground(errColor);
                    return null;
                }
            }

            if (i == tempStr.length())
                amnt++;
            else if (tempStr.charAt(i) == '[')
                amnt += Integer.parseInt(tempStr.substring(i + 1, tempStr.length() - 1));
            else
                amnt += Integer.parseInt(tempStr.substring(i + 1));

            for (int k = 0; k < amnt; k++)
                sequenceArr[j++] = temp;
        }

        Arrays.sort(sequenceArr);
        boolean valid = true;

        //check amount
        try {
            if (Integer.parseInt(amount.getText()) > arraySize || Integer.parseInt(amount.getText()) < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            amount.setBackground(errColor);
            valid = false;
        }
        //check sample size
        try {
            if (sample.getText().length() != 0 && (Integer.parseInt(sample.getText()) > arraySize || Integer.parseInt(amount.getText()) < 0))
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sample.setBackground(errColor);
            valid = false;
        }
        //check specific card
        try {
            if (specific.getText().length() != 0 && Integer.parseInt(specific.getText()) < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            specific.setBackground(errColor);
            valid = false;
        }

        if (valid) {
            sequenceArr[0] = Integer.parseInt(amount.getText());
            sequenceArr[1] = sample.getText().length() == 0 ? arraySize : Integer.parseInt(sample.getText());
            sequenceArr[2] = specific.getText().length() == 0 ? -1 : Integer.parseInt(specific.getText());
            sequenceArr[3] = less.isSelected() ? 1 : 0;
            sequenceArr[4] = equal.isSelected() ? 1 : 0;
            sequenceArr[5] = greater.isSelected() ? 1 : 0;
            sequenceArr[6] = consecutive.isSelected() ? 1 : 0;
            sequenceArr[7] = print.isSelected() ? 1 : 0;
        }

        return valid ? sequenceArr : null;
    }

    private void resetFieldColors() {
        sequence.setBackground(Color.WHITE);
        amount.setBackground(Color.WHITE);
        sample.setBackground(Color.WHITE);
        specific.setBackground(Color.WHITE);
    }

    public JMenuBar createMenuBar() {

        JMenuBar menuBar;
        JMenu file, help;
        JMenuItem exit, manual;

        exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        file = new JMenu("File");
        file.add(exit);

        manual = new JMenuItem("User Manual");
        manual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showManualPanel();
            }
        });

        help = new JMenu("Help");
        help.add(manual);

        menuBar = new JMenuBar();
        menuBar.add(file);
        menuBar.add(help);

        return menuBar;
    }

    private void showManualPanel() {
        ((CardLayout)this.getLayout()).show(this, manualCard);
    }

    private void showMainPanel() {
        ((CardLayout)this.getLayout()).show(this, mainCard);
    }

    private void printToConsole(String s) {
        console.append(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        resetFieldColors();
        int[] info = validSimulation();

        if (info != null) {

            //todo: do print later
            int[] spec = new int[SPEC_SIZE];
            int[] seq = new int[info.length - SPEC_SIZE];
            int i;

            for (i = 0; i < SPEC_SIZE; i++)
                spec[i] = info[i];

            for (int j = 0; j < seq.length; j++, i++)
                seq[j] = info[i];

            String num, den;
            StringTokenizer st = new StringTokenizer(doSimulation(spec, seq), " ");
            StringBuilder consoleOut = new StringBuilder("Unreduced: " + st.nextToken());
            consoleOut.append('/');
            consoleOut.append(st.nextToken());
            num = st.nextToken();
            consoleOut.append(" ~ Reduced: ");
            consoleOut.append(num);
            den = st.nextToken();
            consoleOut.append('/');
            consoleOut.append(den);
            consoleOut.append("\nPercent: ");
            consoleOut.append(new DecimalFormat("#.##").format(((double)Long.parseLong(num) / Long.parseLong(den)) * 100));
            consoleOut.append("%\n\n");
            console.append(consoleOut.toString());
        }

        else console.append("Invalid sequence / specifications\n\n");
    }

    private native String doSimulation(int[] spec, int seq[]);
}

public class StatisticsCalculator {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI(args);
            }
        });
    }

    private static void createAndShowGUI(String[] args) {

        StatisticsDisplay statsDisplay = new StatisticsDisplay(args.length == 0 ? "Enter Sequence Here" : args[0]);

        JFrame frame = new JFrame("Statistics Calculator");
        frame.setSize(900,600);
        frame.getContentPane().add(statsDisplay);
        frame.setJMenuBar(statsDisplay.createMenuBar());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}