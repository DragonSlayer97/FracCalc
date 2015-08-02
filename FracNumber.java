/**
 * A simple value object representing an improper fractions number
 */
public class FracNumber {

	public int numerator;
	public int denominator;
	public FracNumber( int n, int d ){
		numerator = n ;
		denominator = d;
	}
	
	public String toString(){
		return numerator + "/" + denominator ;
	}
}
