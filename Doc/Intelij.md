IntelliJ
Project 의 Open Module Setting 에서  JAVA SDK 설정 및  project language level 설정 가능
shift 키를 두번 누르면 Search Everywhere
컴파일 에러 , warning  등이 표시된 코드에서  option + enter 를 치면  quick fix 가능
ctrl + i  키를 치면  method implement   popup 뜸
ctrl + o 키를 치면 method override popup 뜸
cmd + n 키를 치면 Generate popup 뜸
Constructor , Getter 등을 생성 가능
변수를 선택하고 ctrl + j 키를 치면 타입을 표시해 줌
ctrl + h 키를 치면  type hierarchy 를 표시함
Content Completion 은 처음에 할당된 키가  mac os 의 다른 기능과 겹치기 때문에  preference -> keymap에 들어가서  다른 단축키로 바꾸는 것이 좋음.
Intelli J 는  maven 과 별도의  build system 을 이용하여 build 하고  실행함
Intelli J 에서 컴파일 되었어도 maven 에서 컴파일 되지 않을 수 있음
pom.xml 을 변경하였을 경우  maven window 에서 새로고침 버튼을 클릭하여 intelliJ  build system 에 반영하여야 함

Maven
groupId  는 회사명
artifactId 는 프로젝트 명
외부 라이브러리를 추가할 때는 https://mvnrepository.com 에서 검색하여  maven 탭의 xml 을 복사하여 pom 파일에 붙여넣기 하면 됨