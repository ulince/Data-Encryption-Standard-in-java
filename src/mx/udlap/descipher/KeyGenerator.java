package mx.udlap.descipher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mx.udlap.utilities.Utils;

import org.apache.commons.lang3.ArrayUtils;

public class KeyGenerator {

	/**
	 * @param args
	 */
	
	public KeyGenerator(){
		
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
	
	/*Peforms Permutated Choice - 1 to input key[8][8]*/
	public static boolean[] permutedChoice1(boolean key[][]){
		boolean output[];
		int j;
		boolean keyR[] = reduce(key);
		/*for(boolean item:keyR){
			System.out.print(booleanValue(item));
		}
		*/
		System.out.println("");

		output = new boolean[56];
		for (int i = 0; i < Utils.pc1.length; i++) {
			j = Utils.pc1[i] - 1;
			output[i] = keyR[j];
		}

		return output;
	}
	
	/* Split the input boolean[8][7] into left[0 to 3][7] or left[4 to 7][7] */
	public boolean[][] split(String direction, boolean input[][]) {
		boolean output[][];
		int j;

		output = new boolean[4][7];
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
	
	/*Performs a circular left shift on boolean[] by distance positions*/
	public static boolean[] leftShift(boolean input[], int distance){
		boolean output[];
		
		Boolean toRotate[] = ArrayUtils.toObject(input);		
		List<Boolean> toRotateList = Arrays.asList(toRotate);	
		Collections.rotate(toRotateList, distance * -1);		
		toRotate = (Boolean[])toRotateList.toArray();		
		output = ArrayUtils.toPrimitive(toRotate);
		
		return output;
		
	}
	
	/*Performs a circular left shift on boolean[] by distance positions*/
	public static boolean[] rightShift(boolean input[], int distance){
		boolean output[];
		
		Boolean toRotate[] = ArrayUtils.toObject(input);		
		List<Boolean> toRotateList = Arrays.asList(toRotate);	
		Collections.rotate(toRotateList, distance);		
		toRotate = (Boolean[])toRotateList.toArray();		
		output = ArrayUtils.toPrimitive(toRotate);
		
		return output;
		
	}
	
	/*Performs permuted choice 2. input[56] -> output[48]*/
	public static boolean[] permutedChoice2(boolean input[]){
		boolean output[];
		int j;

		output = new boolean[48];
		for (int i = 0; i < Utils.pc2.length; i++) {
			j = Utils.pc2[i] - 1;
			output[i] = input[j];
		}

		return output;
	}
	
	/*input[0] is c, input[1] is D*/
	public static boolean[] generateKey(boolean input[][], int round){
		int index;
		boolean c[] = input[0];
		boolean d[] = input[1];
		boolean toPermute[];
		boolean permuted[];
		
		toPermute = new boolean[56];
		
		index = 0;
		for(int i = 0; i < 28; i++){
			toPermute[index] = c[i];
			index++;
		}
		
		for(int i = 0; i < 28; i++){
			toPermute[index] = d[i];
			index++;
		}
		
		permuted = permutedChoice2(toPermute);
		
		return permuted;	
	}
	
	public static boolean[][] reverseKey(boolean input[][]){
		boolean output[][];
		int index;
		boolean c[] = input[0];
		boolean d[] = input[1];
		boolean toPermute[];
		boolean permuted[];
			
		output = new boolean[16][48];
		toPermute = new boolean[56];
		
		index = 0;
		for(int i = 0; i < 28; i++){
			toPermute[index] = c[i];
			index++;
		}
		
		for(int i = 0; i < 28; i++){
			toPermute[index] = d[i];
			index++;
		}
		
		permuted = permutedChoice2(toPermute);
		output[0] = permuted;
		
		for(int i = 1; i < 16; i++){
			if(i == 1 | i == 8 | i == 15){
				c = rightShift(c, 1);
				d = rightShift(d, 1);
			}else{
				c = rightShift(c, 2);
				d = rightShift(d, 2);
			}
			
			index = 0;
			for(int j = 0; j < 28; j++){
				toPermute[index] = c[j];
				index++;
			}
			
			for(int k = 0; k < 28; k++){
				toPermute[index] = d[k];
				index++;
			}
			
			permuted = permutedChoice2(toPermute);
			output[i] = permuted;
		}
		
	return output;
		
	}
}
