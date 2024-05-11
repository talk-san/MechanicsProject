public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;
	private int numCourses;
	
	public Autoassociator(CourseArray courses) {
		numCourses = courses.length();
		weights = new int[numCourses][numCourses];
		trainingCapacity = 0;
		for (int i = 0; i < numCourses; i++) {
			for (int j = 0; j < numCourses; j++) {
				weights[i][j] = 0;
			}
		}
	}
	
	public int getTrainingCapacity() {
		return trainingCapacity;
	}
	
	public void training(int pattern[]) {
		// TO DO
		for (int i = 0; i < numCourses; i++) {
			for (int j = 0; j < numCourses; j++) {
				if (i != j) {
					weights[i][j] += (int) (2 * (pattern[i] - 0.5) * (pattern[j] - 0.5));
				}
			}
		}
		trainingCapacity++;
	}
	
	public int unitUpdate(int neurons[]) {
		int index = (int) (Math.random() * numCourses);
		return updateNeuron(neurons, index);
	}

	public void unitUpdate(int[] neurons, int index) {
		updateNeuron(neurons, index);
	}

	private int updateNeuron(int[] neurons, int index) {
		int sum = 0;
		for (int j = 0; j < numCourses; j++) {
			if (j != index) {
				sum += weights[index][j] * neurons[j];
			}
		}
		neurons[index] = sum > 0 ? 1 : -1;
		return index;
	}

	public void chainUpdate(int[] neurons, int steps) {
		for (int i = 0; i < steps; i++) {
			unitUpdate(neurons);
		}
	}

	public void fullUpdate(int[] neurons) {
		boolean stable;
		do {
			stable = true;
			for (int i = 0; i < numCourses; i++) {
				int oldState = neurons[i];
				unitUpdate(neurons, i);
				if (neurons[i] != oldState) stable = false;
			}
		} while (!stable);
	}
}
