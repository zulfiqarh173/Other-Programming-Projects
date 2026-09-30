#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <stdbool.h>

struct lastCommand
{
  int num;
  char command[512]; //we might need to make this a pointer
};


#define ALIASROW 10
void saveAlias(char* array[10][10]);
char* loadAlias(int i, int j);

//void mallocAlias();



int main(void) {
//find user home dir
char* userHome = getenv("HOME");
chdir("..");
//set current working dir to user home dir
chdir(userHome);
char* ogPath = getenv("PATH");
//save current path
char hist_path[600];
snprintf(hist_path, sizeof(hist_path), "%s/.hist_list.txt", userHome);

int count = 0; //count for history array
struct lastCommand history[20]; // history array
for (int i = 0; i < 20; i++) {
	history[i].command[0] = '\0';
	history[i].num = 0;
}

// ***** LOAD HISTORY FROM FILE ***** //
FILE* histptr = fopen(hist_path, "a+");
if (histptr == NULL) {
	printf("Error opening file");
	return -1;
}
// file format number newline command newline number newline
int index;
int temp_count = 0;
while (temp_count < 20 && fscanf(histptr, "%d\n", &index) == 1) {
	if (fgets(history[count].command, 512, histptr) == NULL)
		break;
	
	history[count].num = count + 1;
	count++;
}
fclose(histptr);

// --------------------------------- //

//load history
//load aliases
char* alias[10][10];


for (int i = 0; i < 10; i++){	
	for (int j = 0; j < ALIASROW; j++){
		alias[i][j] = (char *)malloc(sizeof(char*));
		strcpy(alias[i][j],loadAlias(i,j));	
	}
}



char user_in[512]; //we assume at most 512
char line[512]; // copy of userin for history
char* ctrld_check;
while (1) {
	bool from_history = false;
	printf("\n>_< ");
	
	
	ctrld_check = fgets(user_in, 512, stdin);
	if (ctrld_check == NULL || strcmp(user_in, "exit\n")==0) {
		setenv("PATH", ogPath, 1);
		printf("%s \n", getenv("PATH"));
		chdir(userHome);
		saveAlias(alias);
		for (int i = 0; i < 10; i++){
			for (int j = 0; j < 10; j++){
			free(alias[i][j]);
			
			}
		}

		printf("\n");
		return 0;
	}
	// make a copy of history efore token
	strncpy(line, user_in, sizeof(line) - 1);
	line[sizeof(line)-1] = '\0';
	
	char* art[50] = {0}; 
	char* token = strtok(user_in, " |><&;\t\n");

	int i = 0;
	while(token != NULL){
		art[i] = token;
		i += 1;
        token = strtok(NULL, " |><&;\t\n");
	}
	
	
	if (art[0] == NULL){
		continue;
	}
	// history invocation ! shoulds add to history
	if (strcmp(art[0], "!!") == 0) {
		from_history = true;
		if (count == 0) {
			printf("No current history\n");
			continue;
		}
		strcpy(user_in, history[count-1].command);
		
		char temp[512];
		snprintf(temp, sizeof(temp), "%.511s", user_in);
		temp[sizeof(temp)-1] = '\0';
		
		for (int z=0; z<50; z++) art[z] = NULL;
		char* token2 = strtok(temp, " |><&;\t\n");
		int k = 0;
		while (token2 != NULL) {
			art[k++] = token2;
			token2 = strtok(NULL, " |><&;\t\n");
		}
		//we're intentionally allwoing truncanation, compiler gives warning bc it doesnt know this
		snprintf(line, sizeof(line)-1, "%.510s\n", user_in);
		line[sizeof(line)-1] = '\0';
		
	}
	else if (strncmp(art[0], "!-", 2)==0) {
		from_history = true;
		if (count == 0) {
			printf("No current history\n");
			continue;
		}
		int n = atoi(art[0] + 2);
		if (n <= 0) {
			printf("invalid history offset, must be greater than 0 %s\n", art[0]);
			continue;
		}
		if (n > 20) {
			printf("History offset cannot be greater than 20 %s\n", art[0]);
			continue;
		}
		if (n > count) {
			printf("History offset cannot be greater than amount of stored commands: %s\n", art[0]);
			continue;
		}
		strcpy(user_in, history[count-n].command);
		
		char temp[512];
		snprintf(temp, sizeof(temp), "%.511s", user_in);
		temp[sizeof(temp)-1] = '\0';
		
		for (int z=0; z<50; z++) art[z] = NULL;
		char* token2 = strtok(temp, " |><&;\t\n");
		int k = 0;
		while (token2 != NULL) {
			art[k++] = token2;
			token2 = strtok(NULL, " |><&;\t\n");
		}
		snprintf(line, sizeof(line)-1, "%.510s\n", user_in);
		line[sizeof(line)-1] = '\0';
	}	
	else if (art[0][0] == '!' && art[0][1] != '\0') {
		from_history = true;
		if (count == 0) {
			printf("No current history\n");
			continue;
		}
		int n = atoi(art[0] +1);
		if (n <= 0) {
			printf("invalid history number, must be greater than 0: %s\n", art[0]);
			continue;
		}
		if (n > 20) {
			printf("History number cannot be greater than 20 %s\n", art[0]);
			continue;
		}
		if (n > count) {
			printf("History number cannot be greater than amount of stored commands: %s\n", art[0]);
			continue;
		}
		
		strcpy(user_in, history[n-1].command);
		
		char temp[512];
		//snprintf(temp, sizeof(temp)-1, "%s", user_in);
		snprintf(temp, sizeof(temp), "%.511s", user_in);
		temp[sizeof(temp)-1] = '\0';
		
		for (int z=0; z<50; z++) art[z] = NULL;
		char* token2 = strtok(temp, " |><&;\t\n");
		int k = 0;
		while (token2 != NULL) {
			art[k++] = token2;
			token2 = strtok(NULL, " |><&;\t\n");
		}
		snprintf(line, sizeof(line)-1, "%.510s\n", user_in);
		line[sizeof(line)-1] = '\0';
			
	}
	//if (line[0] != '!') 
	if (!from_history) {
		if (count < 20) {
			strncpy(history[count].command, line, 511);
			history[count].command[511] = '\0';
			count++;
		} else {
			for (int j=0; j<19; j++) {
				history[j] = history[j+1];
			}
			strncpy(history[19].command, line, 511);
			history[19].command[511] = '\0';
		}
		for (int j=0; j < count; j++) {
			history[j].num = j+1;
		}
	}
	
	if (strcmp(art[0], "history") == 0) {
	// give error when has too many args, include args in error message
	    if (art[1] != NULL) {
	    	fprintf(stderr, "history: too many args\n");
	    	continue;
	    }
            for (int j=0; j<count;j++) {
            	printf("%d %s", history[j].num, history[j].command);
            }
            continue;
        }
        
	if (strcmp(art[0], "clearhistory") == 0) {
		count = 0;
		for (int i = 0; i < 20; i++) {
			history[i].command[0] = '\0';
			history[i].num = 0;
		}
		continue;
	}
	
	
	//STAGE 7
	
	
	
	for (int i = 0; i < ALIASROW; i++){
		
		if (strlen(alias[i][0])==0){
		continue;
		}
		
	
		if (strcmp(art[0], alias[i][0]) == 0){
						
			for (int j = 1; j < 10; j++){
				if (strlen(alias[i][j])==0){
					continue;	
				}
				art[j-1] = alias[i][j];
			}
		break;	
		}
		
	    	
	}
	

	
	
	// STAGE 4
	if (art[0] && strcmp(art[0], "cd") == 0) {
		//2 or more arguments
		if (art[1] && art[2]) {
        	fprintf(stderr, "cd: too many arguments, only one allowed\n");
        	continue;
    	}
		// jsut typing cd
		if (art[1] == NULL) {
			chdir(getenv("HOME"));
			continue;
		}
		
		// fail, does not exist or is a file
		if (chdir(art[1]) != 0) {
			perror(art[1]);
		}

		continue;
	}

	// STAGE 3
	if (strcmp(art[0], "getpath") == 0){
		if (art[1] == NULL){
			printf("%s", getenv("PATH"));
			
		}
		else {
			printf("getpath : Uses 0 parameters");
		}
		continue;	
	}

	if (strcmp(art[0], "setpath") == 0){
		if (art[1] != NULL && art[2] == NULL){
			
			setenv("PATH", art[1], 1);	
		}
		else{
			printf("setpath : Uses 1 parameter");

		}
			continue;
		}
	
	//stage 7 - continued
	if (strcmp(art[0], "alias") == 0){
		if (art[1] != NULL && art[2] != NULL){
			for (int i = 0; i < ALIASROW; i++){
			//add overwriting aliass attempting to unalias when the list of aliases is empty give an error message?
			
			//|| strcmp(art[1], alias[i][0]) == 0
			//strcmp(alias[i][0],""
			
			
			if (strlen(alias[i][0]) == 0 || strcmp(art[1], alias[i][0]) == 0){
				//printf("\nhello, %s", alias[i][0]);
				int j = 1;
				if(strcmp(art[1], alias[i][0]) == 0){
				printf("you overrided %s", art[1]);
				}
				while (art[j] != NULL&& j<10){
					//if (alias[i][j-1]==NULL){
						//alias[i][j-1]="TEST";
					//}
					
					
					strcpy(alias[i][j-1], art[j]);
					//alias[i][j-1] = art[j];
					j++;
				}
				
				
				
				while (j<=ALIASROW){
					strcpy(alias[i][j-1],"");
					j++;
				}
				
				
				
				
				
			break;
			}
			
			else if (i == ALIASROW-1){
				//printf("%d", i);
				printf("LIST FULL");
				
			}
			
			
			
			
			
		continue;
			
		}


		}
		if (art[1] == NULL){
			for (int i = 0; i < ALIASROW; i++){
				printf("%s : ", alias[i][0]);
				for (int j = 1; j < 10; j++){
					
					printf("%s ,", alias[i][j]);
					
				}
				printf("\n");
				}
		     }
		     if(art[1] != NULL && art[2] == NULL){
		     printf("alias : either takes 0 or 2 parameters");
		     }
		
	continue;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	if (strcmp(art[0], "unalias") == 0){
		if (art[1] == NULL || art[2] != NULL){
			printf("unalias : Uses 1 parameter");
			continue;
		}
		
		
	
		for (int i = 0; i < 10; i++){
			if (strcmp(art[1], alias[i][0]) == 0){
				strcpy(alias[i][0], "");
				for (int j = 1; j < 10; j++){
					if (strlen(alias[i][j])==0){
						continue;
					}
					strcpy(alias[i][j], "");
					//free(alias[i][j]);
				}
				
				break;
			}
			else if(i==ALIASROW-1){
			printf("unalias : couldn't find corrisponding alias"); 
			}
		}
		continue;
	}
	
	
		
	
	
	
	
	
	

	
	
	
	
	pid_t pid = fork();
	if (pid < 0) {
	  fprintf(stderr,"fork failed ):");
	  return 1;
	} 

	else if (pid == 0) // child process
	{
	
		int error_check = execvp(art[0],art);
	  	if (error_check == -1) {
			printf("Command %s caused an error \n", art[0]);
	    	perror("Error");
	    	return 1;
	  }
	} 
	
	else {
	  wait(NULL);
	}

//save history
FILE* hptr = fopen(hist_path, "w");
if (hptr == NULL) {
	printf("Error opening file");
	return -1;
}
// file format number newline command newline number newline
for (int i = 0; i < count; i++) {
    fprintf(hptr, "%d\n%s", history[i].num, history[i].command);
}

fclose(hptr);

}



//save aliases
//restore orginal path
return 0;
}

void saveAlias(char* array[10][10]){
	FILE* fptr;
	
	fptr = fopen(".alias.txt", "w");
	
	if (fptr == NULL) {
        	printf("The file is not opened.");
        	return;
	}
	
	for (int j=0;j<ALIASROW;j++){
		for (int i=0; i < ALIASROW; i++){
		if(i==ALIASROW-1){
			fprintf(fptr, "%s", array[j][i]);
			break;
		}
			fprintf(fptr, "%s,", array[j][i]);
		}
		fprintf(fptr, "\n");
		
		
	}
	
	
	fclose(fptr);
	
}




char* loadAlias(int i,int j){
	FILE* fptr;
	char data[100];
	char* tokenised[10];
	

	
	
	
	fptr = fopen(".alias.txt", "r");
	
	if (fptr == NULL) {
	fptr = fopen(".alias.txt", "w");
	return "";
        	
        	
	}
	int r=0;
	
	while(fgets(data, 100, fptr)) {
  		
  		char* token = strtok(data, ",\n");

		int k = 0;
		while(token != NULL){
		tokenised[k] = token;
		k += 1;
        	token = strtok(NULL, ",\n");
        	
        	if ((k-1)==j&&r==i){
        	fclose(fptr);
		return tokenised[k-1];
        	
        	
        	}
        	
        
        	}
        	
        	r++;
        	
        	
	}
	

	fclose(fptr);
	
	return "";
	
	/*char* art[50] = {0}; 
	char* token = strtok(user_in, " |><&;\t\n");

	int i = 0;
	while(token != NULL){
		art[i] = token;
		i += 1;
        token = strtok(NULL, " |><&;\t\n");
	}*/
	
}
