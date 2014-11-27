package mx.udlap.descipher;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import mx.udlap.utilities.Utils;
import org.apache.commons.lang3.ArrayUtils;

public class DEScipher {

	public static void main(String args[]) {

		boolean plaintext[][] = parseHex(args[0]);
		System.out.print("Plaintext:  ");
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				System.out.print(booleanValue(plaintext[i][j]));
			}
		}
		System.out.println("");

		boolean key[][] = parseHex(args[1]);

		boolean cphtxt[] = cipher(plaintext, key);
		
		

		System.out.print("Ciphertext: ");
		for (int i = 0; i < 64; i++) {
			System.out.print(booleanValue(cphtxt[i]));
		}
		System.out.println("");

		boolean ctxt[][] = new boolean[8][8];

		int index = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				ctxt[i][j] = cphtxt[index];
				index++;
			}
		}

		boolean pltxt[] = decipher(ctxt, key);
		
		/*System.out.print("Plaintext:  ");
		for (int i = 0; i < 64; i++) {
			System.out.print(booleanValue(pltxt[i]));
		}
		System.out.println("");
		*/

	}

	/*Parse hexadecimal string into boolean[][]*/
	public static boolean[][] parseHex(String str) {
		boolean output[][];

		output = new boolean[8][8];
		String binStrings[] = new String[16];
		boolean a[][] = new boolean[16][4];
		output = new boolean[8][8];

		for (int i = 0; i < 16; i++) {
			int data = Integer.parseInt(str.substring(i, i + 1), 16);
			String pad = String.format("%0" + 4 + 'd', 0);
			String s = Integer.toBinaryString(data);
			s = pad.substring(s.length()) + s;
			binStrings[i] = s;
		}

		for (int j = 0; j < 16; j++) {
			for (int i = 0; i < 4; i++) {
				a[j][i] = binStrings[j].charAt(i) == '1' ? true : false;
			}
		}

		output[0] = ArrayUtils.addAll(a[0], a[1]);
		output[1] = ArrayUtils.addAll(a[2], a[3]);
		output[2] = ArrayUtils.addAll(a[4], a[5]);
		output[3] = ArrayUtils.addAll(a[6], a[7]);
		output[4] = ArrayUtils.addAll(a[8], a[9]);
		output[5] = ArrayUtils.addAll(a[10], a[11]);
		output[6] = ArrayUtils.addAll(a[12], a[13]);
		output[7] = ArrayUtils.addAll(a[14], a[15]);

		return output;

	}

	/* Encrypt plaintext */
	public static boolean[] cipher(boolean plaintext[][], boolean key[][]) {
		int index, r;

		/* Prepare plaintext */
		boolean ip[][] = initialPermutation(plaintext);

		boolean l0[][] = split("left", ip);
		boolean r0[][] = split("right", ip);

		boolean l0reduced[] = reduce(l0);
		boolean r0reduced[] = reduce(r0);

		boolean ciphertext[][] = { l0reduced, r0reduced };
		
		/*System.out.print("L: ");
		for(boolean item:ciphertext[0]){
			System.out.print(booleanValue(item));
		}
		System.out.println("");
		
		System.out.print("R: ");
		for(boolean item:ciphertext[1]){
			System.out.print(booleanValue(item));
		}
		System.out.println("");
		*/

		/* Prepare key */
		index = 0;
		boolean pc1[] = KeyGenerator.permutedChoice1(key);
		
		/*for(boolean item:pc1){
			System.out.print(booleanValue(item));
		}
		System.out.println("");
		*/
		
		boolean c[] = new boolean[28];
		boolean d[] = new boolean[28];

		for (int i = 0; i < 28; i++) {
			c[i] = pc1[index];
			index++;
		}
		for (int i = 0; i < 28; i++) {
			d[i] = pc1[index];
			index++;
		}

		boolean nextKey[][] = { c, d };	
		boolean subkey[];

		/* Start rounds */
		r = 16;
		for (int rounds = 0; rounds < r; rounds++) {
			nextKey[0] = KeyGenerator.leftShift(nextKey[0], Utils.lS[rounds]);
			nextKey[1] = KeyGenerator.leftShift(nextKey[1], Utils.lS[rounds]);
			
			subkey = KeyGenerator.generateKey(nextKey, rounds);
			ciphertext = round(ciphertext, subkey);		
		}

		boolean inverse[] = ArrayUtils.addAll(ciphertext[1], ciphertext[0]);
		inverse = inversePermutation(inverse);

		return inverse;
	}

	public static boolean[] decipher(boolean plaintext[][], boolean key[][]) {
		int index, r, k;

		/* Prepare plaintext */
		boolean ip[][] = initialPermutation(plaintext);

		boolean l0[][] = split("left", ip);
		boolean r0[][] = split("right", ip);

		boolean l0reduced[] = reduce(l0);
		boolean r0reduced[] = reduce(r0);

		boolean ciphertext[][] = { l0reduced, r0reduced };

		/* Prepare key */
		index = 0;
		boolean pc1[] = KeyGenerator.permutedChoice1(key);
		boolean c[] = new boolean[28];
		boolean d[] = new boolean[28];

		for (int i = 0; i < 28; i++) {
			c[i] = pc1[index];
			index++;
		}
		for (int i = 0; i < 28; i++) {
			d[i] = pc1[index];
			index++;
		}

		boolean nextKey[][] = { c, d };
		boolean keys[][] = KeyGenerator.reverseKey(nextKey);
		

		/* Start rounds */
		r = 1;
		for (int rounds = 0; rounds < r; rounds++) {
			ciphertext = round(ciphertext, keys[rounds]);
		}

		boolean inverse[] = ArrayUtils.addAll(ciphertext[1], ciphertext[0]);
		inverse = inversePermutation(inverse);

		return inverse;
	}

	/* Each round of the encryption algorithm. input[0] is li, input[1] is ri */
	public static boolean[][] round(boolean input[][], boolean key[]) {
		boolean output[][];

		output = new boolean[2][32];
		boolean l0[] = input[0];
		boolean r0[] = input[1];

		// boolean l0Reduced[] = reduce(l0);
		BitSet l0BitSet = convertToBitSet(l0);

		/* Mangler function */
		boolean r0_2[] = mangler(r0, key);
		BitSet r0_2BitSet = convertToBitSet(r0_2);

		/* XOR */
		BitSet XOR = BitSetXOR(l0BitSet, r0_2BitSet);
		boolean r1[] = convertToBoolArray(XOR, 32);

		output[0] = r0;
		output[1] = r1;

		return output;

	}

	/* Performs mangler function. Returns boolean[32] */
	public static boolean[] mangler(boolean input[], boolean key[]) {

		/* Expansion permutation */
		boolean ep[][] = expansionPermutation(input);
		boolean epReduced[] = reduce(ep);
		
		/*System.out.print("E(R): ");
		for(boolean item:epReduced){
			System.out.print(booleanValue(item));
		}
		System.out.println("");
		*/
		
		BitSet epBitSet = convertToBitSet(epReduced);

		/* XOR with key */
		// boolean keyReduced[] = reduce(key);
		BitSet keyBitSet = convertToBitSet(key);
		BitSet keyXORep = BitSetXOR(epBitSet, keyBitSet);
		boolean keyXep[] = convertToBoolArray(keyXORep, 48);
		
		/*System.out.print("A: ");
		for(boolean item:keyXep){
			System.out.print(booleanValue(item));
		}
		System.out.println("");
		*/

		/* Substitution */
		BitSet sub[] = substitution(keyXep);

		/* Permutation function */
		boolean P[] = permutationP(sub);

		return P;
	}


	/*
	 * Performs Expansion permutation with input[32].Call after
	 * transform(boolean input[][])
	 */
	public static boolean[][] expansionPermutation(boolean input[]) {
		boolean output[][];
		int index, row;

		output = new boolean[8][6];
		index = 0;

		/* Fills index 1 to 4 in every row of a 8 x 6 matrix. Middle bits */
		for (row = 0; row < 8; row++) {
			for (int times = 0; times < 4; times++) {
				output[row][(index % 4) + 1] = input[index];
				index++;
			}
		}

		/*
		 * Write the outer bits for every row in positions 0 and 5 on 8 x 6
		 * matrixDo row 0 and 7 separately
		 */
		output[0][0] = output[7][4];
		output[0][5] = output[1][1];
		output[7][0] = output[6][4];
		output[7][5] = output[0][1];

		for (row = 1; row < 7; row++) {
			output[row][0] = output[row - 1][4];
			output[row][5] = output[row + 1][1];
		}

		return output;

	}


	/* Perform the initial permutation, with input[8][8]. */
	public static boolean[][] initialPermutation(boolean input[][]) {
		boolean even[][] = new boolean[4][8];
		boolean odd[][] = new boolean[4][8];
		boolean output[][] = new boolean[8][8];
		int row, column, x, y;

		x = 0;
		for (column = 0; column < 8; column += 2) {
			y = 0;
			for (row = 7; row >= 0; row--) {
				odd[x][y] = input[row][column];
				y++;
			}
			x++;
		}

		x = 0;
		for (column = 1; column < 8; column += 2) {
			y = 0;
			for (row = 7; row >= 0; row--) {
				even[x][y] = input[row][column];
				y++;
			}
			x++;
		}

		for (row = 0; row < 4; row++) {
			output[row] = even[row];
		}
		x = 0;
		for (row = 4; row < 8; row++) {
			output[row] = odd[x];
			x++;
		}
		return output;
	}
	
	/*
	 * Performs subtitution with Sboxes. Call after BitSetXOR. Input is
	 * boolean[48] Output is BitSet[8]
	 */
	public static BitSet[] substitution(boolean input[]) {
		boolean temp[][];
		int index, row, column;
		BitSet bitset[];

		temp = new boolean[8][6];
		index = 0;
		bitset = new BitSet[8];

		for (row = 0; row < 8; row++) {
			for (column = 0; column < 6; column++) {
				temp[row][column] = input[index];
				index++;
			}
		}

		// Cada bitset debe ser de tamano 4

		for (row = 0; row < 8; row++) {
			// Get outer bits
			int xCoord;
			boolean outerBitsBoolArray[] = { temp[row][0], temp[row][5] };
			BitSet outerBitSet = convertToBitSet(outerBitsBoolArray);
			long outerBitsLongArray[] = outerBitSet.toLongArray();
			if (outerBitsLongArray.length == 0) {
				xCoord = 0;
			} else {
				xCoord = (int) outerBitsLongArray[0];
			}

			// Get middle bits
			int yCoord;
			boolean middleBitsBoolArray[] = { temp[row][1], temp[row][2],
					temp[row][3], temp[row][4] };
			BitSet middleBitSet = convertToBitSet(middleBitsBoolArray);
			long middleBitsLongArray[] = middleBitSet.toLongArray();

			if (middleBitsLongArray.length == 0) {
				yCoord = 0;
			} else {

				yCoord = (int) middleBitsLongArray[0];
			}

			long substitute[] = { Utils.s[row][xCoord][yCoord] };
			bitset[row] = BitSet.valueOf(substitute);
		}

		return bitset;
	}

	/* Perform Permutation function. Return boolean[32] */
	public static boolean[] permutationP(BitSet[] input) {
		boolean temp[];
		boolean output[];
		int row, column, index, j;

		index = 0;
		temp = new boolean[32];
		output = new boolean[32];
		for (row = 0; row < 8; row++) {
			for (column = 3; column >= 0; column--) {
				temp[index] = input[row].get(column);
				index++;
			}
		}

		index = 0;
		for (int i = 0; i < Utils.pf.length; i++) {
			j = Utils.pf[i] - 1;
			output[i] = temp[j];
		}

		return output;
	}

	/* Performs Inverse permutation. input[64] -> output[64] */
	public static boolean[] inversePermutation(boolean input[]) {
		boolean output[];
		int j;

		output = new boolean[64];
		for (int i = 0; i < Utils.invP.length; i++) {
			j = Utils.invP[i] - 1;
			output[i] = input[j];
		}

		return output;
	}

	/* Split the input boolean[8][8] into left[0 to 3][8] or left[4 to 7][8] */
	public static boolean[][] split(String direction, boolean input[][]) {
		boolean output[][];
		int j;

		output = new boolean[4][8];
		if (direction.equals("left")) {
			for (int i = 0; i < 4; i++) {
				output[i] = input[i];
			}
		}

		if (direction.equals("right")) {
			j = 0;
			for (int i = 4; i < 8; i++) {
				output[j] = input[i];
				j++;
			}
		}

		return output;

	}

	/* Convert boolean[] to Bitset */
	public static BitSet convertToBitSet(boolean input[]) {
		BitSet output;
		int index;

		output = new BitSet(input.length);
		index = 0;
		for (int i = input.length - 1; i >= 0; i--) {
			// System.out.println(i + " "+index);
			output.set(index, input[i]);
			index++;
		}

		return output;
	}

	/* Convert BitSet to boolean[size] */
	public static boolean[] convertToBoolArray(BitSet bitset, int size) {
		boolean output[];
		int index;

		output = new boolean[size];
		index = size - 1;
		for (int i = 0; i < size; i++) {
			output[i] = bitset.get(index);
			index--;
		}

		return output;
	}

	/* Performs XOR operation between two BitSets */
	public static BitSet BitSetXOR(BitSet b1, BitSet b2) {
		BitSet output;

		output = (BitSet) b1.clone();
		output.xor(b2);
		return output;
	}
	
	
	/* get int 1 or 0 for boolean b */
	public static int booleanValue(boolean b) {
		int i;
		i = b ? 1 : 0;
		return i;
	}
	
	/*
	 * convert input[rows][columns] into output[rows * columns], call
	 * expansionPermutation later
	 */
	public static boolean[] reduce(boolean input[][]) {
		boolean output[];
		int i;

		output = new boolean[input.length * input[0].length];
		i = 0;

		for (int row = 0; row < input.length; row++) {
			for (int column = 0; column < input[0].length; column++) {
				output[i] = input[row][column];
				i++;
			}
		}

		return output;
	}

}
