import java.io.*;
import java.util.*;

public class Main {
	static int[][] arr;
	static Knight[][] knightArr;
	static Map<Integer, Knight> map;
	static Set<Integer> set = new HashSet<Integer>();
	static int damage;
	// 위 오른쪽 아래쪽 완쪽
	static int[] dirX = { -1, 0, 1, 0 };
	static int[] dirY = { 0, 1, 0, -1 };
	

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st;

		st = new StringTokenizer(br.readLine());
		int l = Integer.parseInt(st.nextToken());

		int n = Integer.parseInt(st.nextToken());
		int q = Integer.parseInt(st.nextToken());

		// 0빈칸, 1함정, 2벽
		arr = new int[l + 1][l + 1];
		knightArr = new Knight[l + 1][l + 1];
		map = new HashMap<>();

		for (int i = 1; i < arr.length; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 1; j < arr[0].length; j++) {
				arr[i][j] = Integer.parseInt(st.nextToken());
			}
		}
		// 초기 기사의 정보
		for (int i = 1; i <= n; i++) {
			st = new StringTokenizer(br.readLine());
			// 기사의 처음 위치 x
			int r = Integer.parseInt(st.nextToken());
			// 기사의 처음위치 y
			int c = Integer.parseInt(st.nextToken());
			// 세로 길이
			int h = Integer.parseInt(st.nextToken());
			// 가로 길이
			int w = Integer.parseInt(st.nextToken());
			// 초기 체력
			int k = Integer.parseInt(st.nextToken());

			Knight knight = new Knight(i, r, c, r + h - 1, c + w - 1, k, k);
			map.put(i, knight);
			//기사 판 다시 깔아주기
			knightArr = new Knight[l + 1][l + 1];
			for (int j = r; j < r + h; j++) {
				for (int j2 = c; j2 < c + w; j2++) {
					knightArr[j][j2] = knight;
				}
			}
		}
		// 왕의 명령
		for (int j = 0; j < q; j++) {
			st = new StringTokenizer(br.readLine());
			// i번 기사에게
			int i = Integer.parseInt(st.nextToken());
			// 방향으로 한칸 이동하라
			int d = Integer.parseInt(st.nextToken());

			// 이동된 좌표값
			int startX = map.get(i).startX + dirX[d];
			int startY = map.get(i).startY + dirY[d];
			int endX = map.get(i).endX + dirX[d];
			int endY = map.get(i).endY + dirY[d];
			
			set.clear();
			moveKnight(startX, startY, endX, endY, i, d);
			
			for (int k = 1; k <= n; k++) {
				if(map.containsKey(k)) {
					Knight knight = map.get(k);
					for (int kk = knight.startX; kk <= knight.endX; kk++) {
						for (int j2 = knight.startY; j2 <= knight.endY; j2++) {
							knightArr[kk][j2] = knight;
						}
					}
				}
				
			}

		}
		// 데미지 계산하기
		for (int k = 1; k <= n; k++) {
			if (map.containsKey(k)) {
				damage += map.get(k).firstHp - map.get(k).lastHp;
			}
		}
		System.out.println(damage);
	}

	static private boolean moveKnight(int startX, int startY, int endX, int endY, int i, int d) {
		if (!isMoved(startX, startY, endX, endY))
			return false;
		// 해당 기사가 이동 가능하다면 이동할 기사가 다음 기사와 겹치는지 확인 후 다음 기사도 이동준비하기
		List<Integer> dupKnightList = findDupKnightList(startX, startY, endX, endY);
		if (!dupKnightList.isEmpty()) {
			for (int k = 0; k < dupKnightList.size(); k++) {
				int number = dupKnightList.get(k);
				int anotherStartX = map.get(number).startX + dirX[d];
				int anotherStartY = map.get(number).startY + dirY[d];
				int anotherEndX = map.get(number).endX + dirX[d];
				int anotherEndY = map.get(number).endY + dirY[d];

				int firstHp = map.get(number).firstHp;
				int lastHp = map.get(number).lastHp;

				set.add(number);
				// 이동된 좌표값이 이동가능한 좌표값들인지 체킹하기.
				boolean isComplete = moveKnight(anotherStartX, anotherStartY, anotherEndX, anotherEndY, i, d);
				if (isComplete) {
					// 이동된 좌표의 함정 개수 체크해서 데미지에 더해주기
					int deleteCount = pushCount(anotherStartX, anotherStartY, anotherEndX, anotherEndY);

					int nextHp = lastHp - deleteCount;

					if (nextHp <= 0) {
						for (int j = map.get(number).startX; j < map.get(number).endX; j++) {
							for (int j2 = map.get(number).startY; j2 < map.get(number).endY; j2++) {
								knightArr[j][j2] = null;
							}
						}
						map.remove(number);
					} else {
						map.replace(number,
								new Knight(number, anotherStartX, anotherStartY, anotherEndX, endY, firstHp, nextHp));
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	static private int pushCount(int startX, int startY, int endX, int endY) {
		int count = 0;
		for (int k = startX; k <= endX; k++) {
			for (int k2 = startY; k2 <= endY; k2++) {
				if (arr[k][k2] == 1) {
					count++;
				}
			}
		}
		return count;

	}

	// 움직인 좌표에 이미 존재하는 나이트틀 리스트 리턴 메소드
	static private List<Integer> findDupKnightList(int startX, int startY, int endX, int endY) {
		List<Integer> knightNumberList = new ArrayList<>();
		for (int k = startX; k <= endX; k++) {
			for (int k2 = startY; k2 <= endY; k2++) {
				if (knightArr[k][k2] != null && !set.contains(knightArr[k][k2].number)) {
					// 나이트가 존재한다.
					knightNumberList.add(knightArr[k][k2].number);
				}
			}
		}
		return knightNumberList;
	}

	static private boolean isMoved(int startX, int startY, int endX, int endY) {
		for (int k = startX; k <= endX; k++) {
			for (int k2 = startY; k2 <= endY; k2++) {
				if (arr[k][k2] == 2) {
					// 벽이 포함되어있으니까 움직이지 않는것으로 결정.
					return false;
				}
			}
		}
		return true;
	}

	static class Knight {
		int number;
		int startX;
		int startY;
		int endX;
		int endY;
		int firstHp;
		int lastHp;

		public Knight(int number, int startX, int startY, int endX, int endY, int firstHp, int lastHp) {
			super();
			this.number = number;
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
			this.firstHp = firstHp;
			this.lastHp = lastHp;
		}
	}

}