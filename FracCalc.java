/* FracCalc Project
 * @date 10-27-2014
 * @class AP CS-A period 1
 * @author Ian Renfro
 */

/* Welcome to the Fraction Calculator
 * 
 * ---------- Public API ----------
 * 
 * public static string calculate(String input)
 * 
 * The Fraction Calculator has a public calculate method 
 * that calculates the string expression that was entered and returns a string
 * 
 * The input format for this class is
 * 
 *     number (space) operator (space) number ...
 * 
 * Valid format for numbers are 1 or 1/2 or 1_1/2
 * Valid operators: + - * /
 * 
 * ---------- Stand alone Main ----------
 *
 * This program also implements a main method.
 * This is used as a stand alone program allowing users to run this program from a command line
 * which takes the user input, in the correct format(see above), then calculates and prints the
 * result 
 *
 * ---------- Internal Structure ----------
 * 
 * This is a Stack based program
 * The program takes the user input and evaluates the high precedence operations right away 
 * while pushing the low precedence operations onto the stack to evaluate later 
 * 
 */

import java.util.Scanner;
import java.util.Stack;

public class FracCalc {

	/**
	 * Main acts as a command line program that takes a user input and prints
	 * out the answer to the expression until the user quits.
	 * 
	 * If the input is invalid then main prints an error message
	 * 
	 * @param args
	 *            unused in this program
	 */
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to the Fraction Calculator!");

		while (true) {
			System.out.println("Enter an equation or quit:");
			String input = sc.nextLine();

			// Quit if that's what the user wants.
			if (input.toLowerCase().equals("quit")) {
				break;
			}

			try {
				// Do the hard work of finding an answer.
				System.out.println(calculateX(input));

			} catch (InvalidInputException e) {
				// Oops; the input must have been invalid.
				System.out.println(e.getMessage());
			}

		}
		System.out.println("Goodbye :)");
	}

	/**
	 * Takes the Input String for the JUnit Tests If it does not passes the test
	 * then it returns null instead of a nice print statement.
	 * 
	 * Callers of this API are not expecting an error message so we just return
	 * null unlike the command line when we want to give the user good feedback
	 * on their mistake
	 * 
	 * @param input
	 *            a String that holds the expression from the user
	 * @return a String with either the answer or null if the input was invalid
	 */
	public static String calculate(String input) {
		try {
			return calculateX(input);
		} catch (InvalidInputException e) {
			// Input was invalid. So we need to return null
			return null;
		}
	}

	/**
	 * Takes a String input from the main or another program Uses a Stack to
	 * copy over low precedence operators and operands and calculates on the
	 * spot high precedence operators and operands
	 * 
	 * Then after all the high precedence operations have been done it goes back
	 * to solves all of the low precedence operations
	 * 
	 * @param input
	 *            a string input, from user or other class, that is an
	 *            arithmetic expression
	 * @return answer to the arithmetic expression
	 * @throws InvalidInputException
	 *             the other methods can throw an InvalidInputException so we
	 *             need to be prepared if it does
	 */
	private static String calculateX(String input) throws InvalidInputException {
		// Creates a new stack and splits the input on spaces
		Stack<String> st = new Stack<String>();
		String[] parts = input.split(" ");

		// Copies low precedence operations (+ or -) to the stack but
		// immediately evaluates high precedence operations (* or /)
		// putting their result on the stack.
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].equals("")) {
				continue;
			}
			if (!st.empty() && (st.peek().equals("*") || st.peek().equals("/"))) {
				// If the stack is not empty and the top value is a high
				// precedence operator
				// then pop off the operator
				// then pop off the left operand
				// Evaluate those immediately and push the result onto the stack
				String operator = st.pop();
				String operand = st.pop();
				st.push(evaluateString(operand, operator, parts[i]));
			} else {
				// If the stack is either empty or it is not a high precedence
				// operator
				// push the next value onto the stack
				st.push(parts[i]);
			}
		}

		// There are now only low precedence operations in the stack
		// This evaluates the rest of the operations
		while (st.size() > 1) {
			String operandR = (String) st.pop();
			String operator = (String) st.pop();
			String operandL = (String) st.pop();
			st.push(evaluateString(operandL, operator, operandR));
		}
		return (String) st.pop();
	}

	/**
	 * This method takes the String representation of the first fraction the
	 * operator and the second fraction then calls helper methods to evaluate
	 * the expression. Returns a simplified and pretty version of the answer
	 * 
	 * @param num1
	 *            The first fraction of the equation
	 * @param operator
	 *            The operator for the equation
	 * @param num2
	 *            The second fraction for the equation
	 * @return The String representation of the answer to the equation
	 * @throws InvalidInputException
	 *             If there is an invalid input we need to be ready to push it
	 *             all the way up to the main
	 */
	private static String evaluateString(String num1, String operator,
			String num2) throws InvalidInputException {

		// Creates FracNumber objects of the fractions
		FracNumber one = stringToFracNumber(num1);
		FracNumber two = stringToFracNumber(num2);

		// Combines the two FracNumbers
		FracNumber result = execute(one, two, operator);

		// Simplifies the result
		result = simplify(result.numerator, result.denominator);

		// Returns a nice looking String representation of result
		return resultToString(result);

	}

	/**
	 * This method takes a string representation of a mixed number or a fraction
	 * or an integer and returns a FracNumber of the string representation in
	 * the form of a fraction
	 * 
	 * @param mix
	 *            String representation of a valid number
	 * @return FracNumber representation of the String
	 * @throws InvalidInputException
	 *             in case we have an InvalidInput like alphanumeric characters
	 */
	private static FracNumber stringToFracNumber(String mix)
			throws InvalidInputException {
		try {
			// Declarations
			int whole = 0;
			String fraction = "";
			String[] parts = mix.split("_");
			FracNumber result;

			// If the user input is negative
			int sign = 1;
			if (parts[0].charAt(0) == '-') {
				sign = -1;
				parts[0] = parts[0].substring(1);
			}

			// If there is no whole number then just copy over the fraction part
			if (parts.length == 1) {
				fraction = parts[0];

				// If there is a whole number
			} else if (parts.length == 2) {
				// Copy the whole number part over
				whole = Integer.parseInt(parts[0]);
				// Copy the fraction portion that the user has entered
				fraction = parts[1];
			} else {
				// If it is not either of these then it must be an InvalidNumber
				throw new InvalidNumberException(mix);
			}

			// Splice the fraction into the numerator and denominator
			String[] fraction_parts = fraction.split("/");
			if (fraction_parts.length == 1) {
				// Just a whole number. The result is over one
				result = new FracNumber(Integer.parseInt(fraction_parts[0]), 1);

			} else if (fraction_parts.length == 2) {
				// Have a fraction. Use the numerator and denominator
				result = new FracNumber(Integer.parseInt(fraction_parts[0]),
						Integer.parseInt(fraction_parts[1]));

			} else {
				// If the number is not one of these then it must be an
				// InvalidNumber
				throw new InvalidNumberException(fraction);
			}

			// Combine the whole number and the fraction
			result.numerator += whole * result.denominator;
			result.numerator *= sign;
			return result;

		} catch (NumberFormatException e) {
			// Integer.parseInt() throws NumberFormatException on invalid input
			throw new InvalidNumberException(mix);
		}

	}

	/**
	 * This method does the hard work of combining the two FracNumber
	 * representation numbers from the user input and returns a single
	 * FracNumber
	 * 
	 * @param left
	 *            the left operand of the equation
	 * @param right
	 *            the right operand of the equation
	 * @param operator
	 *            the operator for the two FracNumbers
	 * @return a result FracNumber
	 * @throws InvalidInputException
	 *             in case we try to divide by 0 or if the user inputed an
	 *             InvalidOperator
	 */
	private static FracNumber execute(FracNumber left, FracNumber right,
			String operator) throws InvalidInputException {

		// Declarations
		FracNumber result;
		int commonDem = 0;

		// If either denominator is 0 then tell the user
		if (left.denominator == 0 || right.denominator == 0) {
			throw new InvalidInputException("Divide by Zero");
		}

		// If we are adding the two numbers
		if (operator.equals("+")) {
			// Make a common denominator
			// Apply the common denominator to the two numerators by
			// multiplying by the other denominator
			commonDem = left.denominator * right.denominator;
			left.numerator *= right.denominator;
			right.numerator *= left.denominator;

			// Simplify the result
			result = simplify(left.numerator + right.numerator, commonDem);

			return result;
		}

		// If we are subtracting the two numbers
		if (operator.equals("-")) {
			// Make a common denominator
			// Apply the common denominator to the two numerators by
			// multiplying by the other denominator
			commonDem = left.denominator * right.denominator;
			left.numerator *= right.denominator;
			right.numerator *= left.denominator;

			// Simplify the result
			result = simplify(left.numerator - right.numerator, commonDem);

			return result;
		}

		// If we are multiplying the two numbers
		if (operator.equals("*")) {
			// Create a new numerator that is the two numerators multiplied
			// together
			// Create a new denominator that is the two denominators multiplied
			// together
			int numerator = left.numerator * right.numerator;
			int denominator = left.denominator * right.denominator;

			// Simplify the result
			result = simplify(numerator, denominator);

			return result;
		}

		// If we are dividing the two numbers
		if (operator.equals("/")) {
			// If the right numerator is zero then when we do the division we
			// will be dividing by zero
			if (right.numerator != 0) {
				// Simplify the result of one numerator multiplied by the other
				// denominator
				// and the other numerator by the other denominator
				result = simplify(left.numerator * right.denominator,
						left.denominator * right.numerator);
				return result;

			} else {
				// If the right hand numerator is 0 then it is an InvalidNumber
				throw new InvalidInputException("Divide by Zero");
			}
		}

		// If none of the operators match then it is an InvalidOperator
		throw new InvalidOperatorException(operator);
	}

	/**
	 * This method takes a numerator and a denominator and returns a simplified
	 * version of the fraction
	 * 
	 * @param numerator
	 *            the numerator for the fraction
	 * @param denominator
	 *            the denominator for the fraction
	 * @return A FracNumber representation of the simplified fraction
	 */
	private static FracNumber simplify(int numerator, int denominator) {
		// Create the greatest common multiple
		int gcf = gcf(numerator, denominator);

		// Create a result FracNumber that is the numerator and the denominator
		// each divided by the greatest common factor
		FracNumber result = new FracNumber((numerator / gcf),
				(denominator / gcf));

		return result;
	}

	/**
	 * This method takes a numerator and a denominator and returns the greatest
	 * common multiple of the two numbers
	 * 
	 * @param numerator
	 *            the numerator for the fraction
	 * @param denominator
	 *            the denominator for the fraction
	 * @return the greatest common factor for the fraction
	 */
	private static int gcf(int numerator, int denominator) {
		// If the denominator is 0 then we have found our greatest common
		// factor
		if (denominator == 0) {
			return numerator;
		} else {
			// If it is not 0 then we need to keep going through our denominator
			// and numerator recursively till the denominator is 0
			return gcf(denominator, numerator % denominator);
		}
	}

	/**
	 * This method takes a FracNumber and returns a pretty String representation
	 * of the fraction and also prints out mixed fractions instead of improper
	 * fractions
	 * 
	 * @param last
	 *            the FracNumber that is the answer to the expression from the
	 *            user
	 * @return a String representation of the FracNumber
	 */
	private static String resultToString(FracNumber last) {

		// If the denominator is 1 then make the String output look pretty
		// by just returning the numerator
		if (last.denominator == 1) {
			return Integer.toString(last.numerator);
		}

		// Since the denominator is not 0
		// we need to check to see if we have an improper fraction
		// If so then we need to take the whole number out of the numerator
		int whole = (last.numerator / last.denominator);
		last.numerator = (last.numerator % last.denominator);

		// If both the whole number and the numerator are negative
		// then we need to keep the negative with the whole number
		// and make the numerator positive
		if (whole < 0 && (last.numerator < 0)) {
			last.numerator *= -1;
		}

		// If the whole number and the denominator are negative
		// then keep the whole number negative and make the
		// denominator positive
		if (whole < 0 && (last.denominator < 0)) {
			last.denominator *= -1;
		}

		// If there is a whole number part to the fraction
		// then return a string concatenation of the whole
		// number an _ and the fraction
		if (whole != 0) {
			return whole + "_" + last;
		}
		// If the denominator is negative then
		// make the denominator positive and the numerator negative
		if (last.denominator < 0) {
			last.denominator *= -1;
			last.numerator *= -1;
		}

		// If there is no whole number part then
		// return the fraction
		return last.toString();
	}

	public static class InvalidInputException extends Exception {
		// New Exception To handle Invalid Inputs and gives the user
		// a helpful error message
		public InvalidInputException(String message) {
			super(message);
		}
	}

	public static class InvalidOperatorException extends InvalidInputException {
		// New Exception to handle InvalidOperators and gives the user
		// a helpful error message
		public InvalidOperatorException(String operator) {
			super(operator + " is not a valid operator."
					+ "\nValid operators: + or - or * or /");
		}
	}

	public static class InvalidNumberException extends InvalidInputException {
		// New Exception to handle InvalidNumbers and gives the user
		// a helpful error message
		public InvalidNumberException(String number) {
			super(
					number
							+ " is not a valid number. "
							+ "\nCorrect format: number (space) operator (space) number ..."
							+ "\nValid format of numbers: 1 or 1/2 or 1_1/2");
		}
	}

}
