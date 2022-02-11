#pragma once
#ifndef SCORE_H
#define SCORE_H

/*
������
Curve 2
Marker 1

�ɾͣ�
1.	�״ε�½       100
2.	�״δ�ͼ��   100
3.	�״λ���       100
4.	�״ε��       100
5.	����100��      500
6.	���100��      300
7.

ÿ������
1.	�״ε�½       20
2.	�״δ�ͼ��   20
3.	�״λ���       20
4.	�״ε��       20
5.

*/

class Score {
private:
	int id;
	int score;
	int curveNum;
	int markerNum;
	int lastLoginYear;
	int lastLoginDay;
	int curveNumToday;
	int markerNumToday;
	int editImageNum;
	int editImageNumToday;

	int achievementsScore[7] = { -1, 100, 100, 100, 100, 500, 300 };
	int dailyQuestsScore[5] = { -1, 20, 20, 20, 20 };

public:
	void drawACurve();
	void pinpoint();
	void openImage();
	void achievementFinished(int i);
	void dailyQuestFinished(int i);
	Score();
	~Score();
	
	int getScore() { return score; }
	int getCurveNum() { return curveNum; }
	int getMarkerNum() { return markerNum; }
	int getLastLoginYear() { return lastLoginYear; }
	int getLastLoginDay() { return lastLoginDay; }
	int getCurveNumToday() { return curveNumToday; }
	int getMarkerNumToday() { return markerNumToday; }
	int getEditImageNum() { return editImageNum; }
	int getEditImageNumToday() { return editImageNumToday; }
};
#endif