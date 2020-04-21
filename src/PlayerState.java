public class PlayerState {

	char[] availableLetters;
	char[] guessedLetters;
	int positionOfGuess;
	int lengthOfWord;

	public char[] getAvailableLetters() {
		return availableLetters;
	}

	public void setAvailableLetters(char[] availableLetters) {
		this.availableLetters = availableLetters;
	}

	public char[] getGuessedLetters() {
		return guessedLetters;
	}

	public void setGuessedLetters(char[] guessedLetters) {
		this.guessedLetters = guessedLetters;
	}

}
