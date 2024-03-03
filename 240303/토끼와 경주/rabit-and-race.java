import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Main {
	static int n, m, p;
	static Map<Integer, Rabit> map = new HashMap<>();
	static ArrayList<Rabit> rabitList;

	public static void main(String[] args) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;
		// 명령의 수
		int q = Integer.parseInt(br.readLine());
		for (int qq = 0; qq < q; qq++) {
			st = new StringTokenizer(br.readLine());
			// 100 경주 시작 준비
			// 200 경주 진행
			// 300 이동거리 변경
			// pid 토끼에 대해 이동거리 l배로 변경
			// 400 최고의 토끼 선정
			// 400명령어에 대해 P마리의 토끼의 최종 점수 중 최댓값을 출력합니다.
			int status = Integer.parseInt(st.nextToken());
			if (status == 100) {
				n = Integer.parseInt(st.nextToken());
				m = Integer.parseInt(st.nextToken());

				p = Integer.parseInt(st.nextToken());

				// jumpCount배열 크기 초기세팅
				rabitList = new ArrayList<Rabit>();
				// 토끼 초기세팅
				for (int i = 0; i < p; i++) {
					int pid = Integer.parseInt(st.nextToken());
					int d = Integer.parseInt(st.nextToken());
					Rabit rabit = new Rabit(pid, 0, 0, d, 0, 0);
					map.put(pid, rabit);
					rabitList.add(rabit);
				}

			} else if (status == 200) {
				// k번 경기 진행
				// k번의 턴 동안 우선순위가 높은 토끼를 뽑아 멀리 보내주는것을 반복
				int k = Integer.parseInt(st.nextToken());
				for (int i = 0; i < k; i++) {
					// 이동할 토끼 선정
					int rabitPid = pickOneRabit();
					// 이동하기 후 나머지에 뿌릴 점수 리턴
					int rc = moveRabit(rabitPid) + 2;

					for (int j = 0; j < rabitList.size(); j++) {
						if (rabitList.get(j).pid == rabitPid) {
							// 이동 값 세팅
							int tempPid = rabitList.get(j).pid;
							rabitList.set(j, map.get(tempPid));
						} else {
							// i번토끼를 제외한 나머지 토끼들은 r+c 점수를 동시에 얻게 된다.
							// 이동하는 토끼 아니면 점수에rc 더해주기
							rabitList.get(j).score += rc;
						}
					}
				}
				int s = Integer.parseInt(st.nextToken());
//				K번의 턴이 모두 진행된 직후에는 
//				(현재 서있는 행 번호 + 열 번호가 큰 토끼, 행 번호가 큰 토끼, 열 번호가 큰 토끼, 고유번호가 큰 토끼) 
//				순으로 우선순위를 두었을 때 가장 우선순위가 높은 토끼를 골라 점수 S를 더해주게 됩니다.
//				K번의 턴 동안 한번이라도 뽑혔던 적이 있던 토끼 중 가장 우선순위가 높은 토끼를 골라야만 함
				// 마지막에 우선순위높은것 추가점수
				int finalPid = finalRabit();
				for (int i = 0; i < rabitList.size(); i++) {
					if (rabitList.get(i).pid == finalPid) {
						rabitList.get(i).score += s;
						map.put(finalPid, rabitList.get(i));
					}
				}
			} else if (status == 300) {
				// 이동거리 변경할 pid
				int pid = Integer.parseInt(st.nextToken());
				// 변경배수
				int l = Integer.parseInt(st.nextToken());
				Rabit rabit = map.get(pid);
				rabit.d = rabit.d * l;
				map.put(pid, rabit);

			} else if (status == 400) {
				// 끝내는로직
				// 마지막 명령
				// 경주를 진행하며 얻은 점수 중 가장 높은 점수 출력하기
				long scoreMax = Integer.MIN_VALUE;
				for (int i = 0; i < rabitList.size(); i++) {
					scoreMax = Math.max(scoreMax, rabitList.get(i).score);
				}
				System.out.println(scoreMax);
			}
		}

	}

	private static int finalRabit() {
		// 한번이라도 뽑혔던 적이 있던 토끼
		// (현재 서있는 행 번호 + 열 번호가 큰 토끼, 행 번호가 큰 토끼, 열 번호가 큰 토끼, 고유번호가 큰 토끼)
		// 우선순위가 높은 토끼를 골라 점수 S를 더해주게 됩니다.

		ArrayList<Rabit> rabit = new ArrayList<>();
		ArrayList<tempCheck> checkList = new ArrayList<>();
		for (int i = 0; i < rabitList.size(); i++) {
			if (rabitList.get(i).score != 0) {
				rabit.add(rabitList.get(i));
				checkList.add(new tempCheck(rabitList.get(i).pid, rabitList.get(i).x + rabitList.get(i).y,
						rabitList.get(i).x, rabitList.get(i).y));
			}
		}
		tempCheck pickRabit = findFirst(checkList);
		return pickRabit.pid;
	}

	// 하 상 우 좌
	static int[] dirX = { 1, -1, 0, 0 };
	static int[] dirY = { 0, 0, 1, -1 };

	/**
	 * 상하좌우 체크해서 이동시키고 r+c를 더한 값 리턴
	 * 
	 * @param pid
	 * @return r+c
	 */
	private static int moveRabit(int pid) {
//		2. 상하좌우 네 방향으로 각각 di만큼 이동했을 때의 위치를 구합니다
//		3. 이동하는 도중 그 다음 칸이 격자를 벗어나게 된다면 방향을 반대로 바꿔 한 칸 이동하
//		4개의 위치 중 (행 번호 + 열 번호가 큰 칸, 행 번호가 큰 칸, 열 번호가 큰 칸) 순으로 우선순위를 두었을 때 
//		가장 우선순위가 높은 칸을 골라 그 위치로 해당 토끼를 이동시킵니다.
		Rabit rabit = map.get(pid);
		int x = rabit.x;
		int y = rabit.y;
		int move = rabit.d;

		ArrayList<tempCheck> nextList = new ArrayList<>();
		for (int i = 0; i < dirX.length; i++) {
			int nextX = Math.abs(x + dirX[i] * move);
			int nextY = Math.abs(y + dirY[i] * move);

			nextX = realDir(nextX, n);
			nextY = realDir(nextY, m);
			nextList.add(new tempCheck(pid, nextX + nextY, nextX, nextY));
		}
		tempCheck pickRabit = findFirst(nextList);
		map.get(pid).moveCount++;
		map.get(pid).x = pickRabit.x;
		map.get(pid).y = pickRabit.y;
		return pickRabit.sumNum;
	}

	private static tempCheck findFirst(ArrayList<tempCheck> nextList) {
//		행 번호 + 열 번호가 큰 칸, 행 번호가 큰 칸, 열 번호가 큰 칸 순으로 우선순위
		ArrayList<tempCheck> checkList = new ArrayList<>();
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < nextList.size(); i++) {
			if (nextList.get(i).sumNum > max) {
				max = nextList.get(i).sumNum;
				checkList = new ArrayList<>();
				checkList.add(nextList.get(i));
			} else if (nextList.get(i).sumNum == max) {
				checkList.add(nextList.get(i));
			}
		}
		if (checkList.size() == 1) {
			return checkList.get(0);
		} else {
			ArrayList<tempCheck> checkList2 = new ArrayList<>();
			int max2 = Integer.MIN_VALUE;
			for (int i = 0; i < checkList.size(); i++) {
				if (checkList.get(i).x > max2) {
					max2 = checkList.get(i).sumNum;
					checkList2 = new ArrayList<>();
					checkList2.add(checkList.get(i));
				} else if (checkList.get(i).x == max2) {
					checkList2.add(checkList.get(i));
				}
			}
			if (checkList2.size() == 1) {
				return checkList2.get(0);
			} else {
				ArrayList<tempCheck> checkList3 = new ArrayList<>();
				int max3 = Integer.MIN_VALUE;
				for (int i = 0; i < checkList2.size(); i++) {
					if (checkList2.get(i).x > max3) {
						max3 = checkList2.get(i).sumNum;
						checkList3 = new ArrayList<>();
						checkList3.add(checkList2.get(i));
					} else if (checkList2.get(i).x == max3) {
						checkList3.add(checkList2.get(i));
					}
				}
				return checkList3.get(0);
			}
		}
	}

	static class tempCheck {
		int pid;
		int sumNum;
		int x;
		int y;

		public tempCheck(int pid, int sumNum, int x, int y) {
			super();
			this.pid = pid;
			this.sumNum = sumNum;
			this.x = x;
			this.y = y;
		}

	}

	/**
	 * 좌표값 벗어나는 경우 올바른 값으로 세팅해주는 메소드
	 * 
	 * @param index
	 * @param maxNum
	 * @return
	 */
	private static int realDir(int index, int maxNum) {
		if (index > maxNum-1) {
			int startDir = index / (maxNum - 1);
			int num = index % (maxNum - 1);
			if (startDir % 2 == 0) {
				// 앞에서부터 진행
				return num;
			} else {
				// 뒤에서부터진행
				return (maxNum - 1) - num;
			}
		}
		return index;
	}

	/**
	 * 경주 진행 중 우선순위 가장 높은 토끼 pid return
	 * 
	 * @return rabit pid
	 */
	private static int pickOneRabit() {
//		1. 우선순위는 순서대로 (현재까지의 총 점프 횟수가 적은 토끼, 
		int min = Integer.MAX_VALUE;
		ArrayList<Rabit> tempList = new ArrayList<>();

		for (int i = 0; i < rabitList.size(); i++) {
			if (rabitList.get(i).moveCount < min) {
				min = rabitList.get(i).moveCount;
				tempList = new ArrayList<>();
				tempList.add(rabitList.get(i));
			} else if (rabitList.get(i).moveCount == min) {
				tempList.add(rabitList.get(i));
			}
		}
		if (tempList.size() == 1) {
			// 우선순위 선정
			return tempList.get(0).pid;
		} else {
//			현재 서있는 행 번호 + 열 번호가 작은 토끼, 
			ArrayList<Rabit> tempList2 = new ArrayList<>();
			int min2 = Integer.MAX_VALUE;
			for (int i = 0; i < tempList.size(); i++) {
				if (tempList.get(i).x + tempList.get(i).y < min2) {
					min2 = tempList.get(i).moveCount;
					tempList2 = new ArrayList<>();
					tempList2.add(tempList.get(i));
				} else if (tempList.get(i).x + tempList.get(i).y == min2) {
					tempList2.add(tempList.get(i));
				}
			}
			if (tempList2.size() == 1) {
				// 우선순위 선정 2
				return tempList2.get(0).pid;
			} else {
//				행 번호가 작은 토끼, 

				ArrayList<Rabit> tempList3 = new ArrayList<>();
				int min3 = Integer.MAX_VALUE;
				for (int i = 0; i < tempList2.size(); i++) {
					if (tempList2.get(i).x < min3) {
						min3 = tempList2.get(i).moveCount;
						tempList3 = new ArrayList<>();
						tempList3.add(tempList2.get(i));
					} else if (tempList2.get(i).x == min3) {
						tempList3.add(tempList2.get(i));
					}
				}
				if (tempList3.size() == 1) {
					// 우선순위 선정 2
					return tempList3.get(0).pid;
				} else {
//					열 번호가 작은 토끼, 
					ArrayList<Rabit> tempList4 = new ArrayList<>();
					int min4 = Integer.MAX_VALUE;
					for (int i = 0; i < tempList3.size(); i++) {
						if (tempList3.get(i).y < min4) {
							min4 = tempList3.get(i).moveCount;
							tempList4 = new ArrayList<>();
							tempList4.add(tempList3.get(i));
						} else if (tempList3.get(i).y == min4) {
							tempList4.add(tempList3.get(i));
						}
					}
					if (tempList4.size() == 1) {
						// 우선순위 선정 3
						return tempList4.get(0).pid;
					} else {
//					고유번호가 작은 토끼)
						int pid = Integer.MAX_VALUE;
						for (int i = 0; i < tempList4.size(); i++) {
							if (tempList4.get(i).pid < pid) {
								pid = tempList4.get(i).pid;
							}
						}
						return pid;
					}

				}
			}

		}
	}

	static class Rabit {
		int pid;
		int x;
		int y;
		int d;
		int moveCount;
		long score;

		public Rabit(int pid, int x, int y, int d, int moveCount, long score) {
			super();
			this.pid = pid;
			this.x = x;
			this.y = y;
			this.d = d;
			this.moveCount = moveCount;
			this.score = score;
		}

	}
}