/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.ru.cmbi.vase.analysis;

public class CmaScore implements Comparable {
	private int residue1;
	private int residue1SpecialNumber;
	private int residue2;
	private int residue2SpecialNumber;
	private double score;

	public int getResidue1() {
		return residue1;
	}

	public void setResidue1(int residue1) {
		this.residue1 = residue1;
	}

	public int getResidue2() {
		return residue2;
	}

	public void setResidue2(int residue2) {
		this.residue2 = residue2;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof CmaScore) {
			CmaScore cma = (CmaScore) o;
			if (score == cma.getScore()) {
				return 0;
			}
			if (score > cma.getScore()) {
				return 1;
			} else {
				return -1;
			}
		}
		return -1;
	}

	public int getResidue1SpecialNumber() {
		return residue1SpecialNumber;
	}

	public void setResidue1SpecialNumber(int residue1SpecialNumber) {
		this.residue1SpecialNumber = residue1SpecialNumber;
	}

	public int getResidue2SpecialNumber() {
		return residue2SpecialNumber;
	}

	public void setResidue2SpecialNumber(int residue2SpecialNumber) {
		this.residue2SpecialNumber = residue2SpecialNumber;
	}

}
