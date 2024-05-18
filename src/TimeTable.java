import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TimeTable extends JFrame implements ActionListener {

    private final JPanel screen = new JPanel();
    private final JPanel tools = new JPanel();
    private JButton[] tool;
    private JTextField[] field;
    private CourseArray courses;
    private Autoassociator autoassociator;
    private int min;
    private int step;
    private final Color[] CRScolor = {Color.RED, Color.GREEN, Color.BLACK};

    public TimeTable() {
        super("Dynamic Time Table");
        setSize(800, 800);
        setLayout(new BorderLayout());

        screen.setPreferredSize(new Dimension(400, 800));
        add(screen, BorderLayout.WEST);

        setTools();
        add(tools, BorderLayout.EAST);

        min = Integer.MAX_VALUE;
        step = 0;

        setVisible(true);
    }

    public void setTools() {
        String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        field = new JTextField[capField.length];

        String capButton[] = {"Load", "Start", "Step", "Print", "Exit", "Continue"};
        tool = new JButton[capButton.length];

        tools.setLayout(new GridLayout(capField.length + capButton.length, 1));

        for (int i = 0; i < field.length; i++) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.add(new JLabel(capField[i]));
            field[i] = new JTextField(10);
            panel.add(field[i]);
            tools.add(panel);
        }

        for (int i = 0; i < tool.length; i++) {
            tool[i] = new JButton(capButton[i]);
            tool[i].addActionListener(this);
            tools.add(tool[i]);
        }

        field[0].setText("10");
        field[1].setText("184");
        field[2].setText("ute-s-92.stu");
        field[3].setText("1");
    }

    public void draw() {
        Graphics g = screen.getGraphics();
        int width = Integer.parseInt(field[0].getText()) * 10;
        for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
            g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(CRScolor[CRScolor.length - 1]);
            g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
        }
    }

    private int getButtonIndex(JButton source) {
        int result = 0;
        while (source != tool[result]) result++;
        return result;
    }

    public void actionPerformed(ActionEvent click) {
        String numOfSlots = field[0].getText();
        String numOfCourses = field[1].getText();
        String clashFileName = field[2].getText();
        String numOfIters = field[3].getText();
        String numOfShifts = field[4].getText();
        int clashes;

        switch (getButtonIndex((JButton) click.getSource())) {
            case 0: // LOAD
                int slots = Integer.parseInt(numOfSlots);
                courses = new CourseArray(Integer.parseInt(numOfCourses) + 1, slots);
                courses.readClashes(clashFileName);
                this.autoassociator = new Autoassociator(courses); // Initialize
                trainAutoassociator(); // Training when loading
                draw();
                break;
            case 1: // START
                // Added a check here to make sure Shift is not empty
                min = Integer.MAX_VALUE;
                step = 0;
                if (!numOfShifts.isEmpty()) {
                    for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
                    for (int iteration = 1; iteration <= Integer.parseInt(numOfIters); iteration++) {
                        courses.iterate(Integer.parseInt(numOfShifts));
                        applyAutoassociatorUpdates();
                        draw();
                        clashes = courses.clashesLeft();
                        if (clashes < min) {
                            min = clashes;
                            step = iteration;
                        }
                    }
                    System.out.println("Shift = " + numOfShifts + "\tMin clashes = " + min + "\tat step " + step);
                    setVisible(true);
                } else {
                    System.out.println("Shift field is empty. Please enter a value.");
                }
                break;
            case 2: // STEP
                courses.iterate(Integer.parseInt(numOfShifts));
                draw();
                break;
            case 3: // PRINT
                System.out.println("Exam\tSlot\tClashes");
                for (int i = 1; i < courses.length(); i++)
                    System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
                break;
            case 4:
                System.exit(0);
            case 5: // CONTINUE
                // Same as start with current step count.
                if (!numOfShifts.isEmpty()) {
                    for (int iteration = 1; iteration <= Integer.parseInt(numOfIters); iteration++) {
                        courses.iterate(Integer.parseInt(numOfShifts));
                        applyAutoassociatorUpdates();
                        draw();
                        clashes = courses.clashesLeft();
                        if (clashes < min) {
                            min = clashes;
                            step = iteration;
                        }
                    }
                    System.out.println("Shift = " + numOfShifts + "\tMin clashes = " + min + "\tat step " + step);
                    setVisible(true);
                } else {
                    System.out.println("Shift field is empty. Please enter a value.");
                }
                break;
        }
    }

    public void trainAutoassociator() {
        String numOfSlots = field[0].getText();
        for (int i = 0; i < Integer.parseInt(numOfSlots); i++) {
            int[] timeslotData = courses.getTimeSlot(i);
            if (isClashFree(timeslotData)) {
                autoassociator.training(timeslotData);
            }
        }
    }

    private boolean isClashFree(int[] timeslotData) {
        for (int i = 0; i < timeslotData.length; i++) {
            if (timeslotData[i] == 1) {
                if (courses.maxClashSize(i) > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void applyAutoassociatorUpdates() {
        for (int i = 1; i < courses.length(); i++) {
            int[] currentTimeslot = courses.getTimeSlot(courses.slot(i));
            int suggestedIndex = autoassociator.unitUpdate(currentTimeslot);
            if (suggestedIndex != courses.slot(i)) {
                courses.setSlot(i, suggestedIndex);
            }
        }
        draw();
    }

    public static void main(String[] args) {
        new TimeTable();
    }
}
