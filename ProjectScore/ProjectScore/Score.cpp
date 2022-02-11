#include "Score.h"
#include <ctime>
using namespace std;

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

Score::Score() {
	// read from database to init
	// ...
	
	time_t now = time(0);                                      
	// char *dt = ctime(&now);
	tm gmtm;
	gmtime_s(&gmtm, &now);
	int nowYear = gmtm.tm_year;
	int nowDay = gmtm.tm_yday;
	
	if (nowDay != lastLoginDay || nowYear != lastLoginYear) {
		curveNumToday = 0;
		markerNumToday = 0;
		dailyQuestFinished(1);
	}

	if (lastLoginDay == -1 && lastLoginYear == -1) {
		achievementFinished(1);
	}

	lastLoginDay = nowDay;
	lastLoginYear = nowYear;

}

Score::~Score() {
	// update database
	// ...
}

void Score::drawACurve(){
	if (curveNum == 0) {
		achievementFinished(1);
	}

	if (curveNumToday == 0) {
		dailyQuestFinished(3);
	}

	if (curveNum == 99) {
		achievementFinished(5);
	}

	curveNum += 1;
	curveNumToday += 1;
	score += 2;
}

void Score::pinpoint() {
	if (markerNum == 0) {
		achievementFinished(2);
	}

	if (markerNumToday == 0) {
		dailyQuestFinished(4);
	}

	if (markerNum == 99) {
		achievementFinished(6);
	}

	markerNum += 1;
	markerNumToday += 1;
	score += 1;
}

void Score::openImage() {
	if (editImageNum == 0) {
		achievementFinished(1);
	}

	if (editImageNumToday == 0) {
		dailyQuestFinished(1);
	}

	editImageNum += 1;
	editImageNumToday += 1;
}

void Score::achievementFinished(int i) {
	score += achievementsScore[i];

	// send achievement finsished message
	// ...
}

void Score::dailyQuestFinished(int i) {
	score += dailyQuestsScore[i];

	// send daily quest finished message
	// ...
}