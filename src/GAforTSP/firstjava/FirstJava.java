package GAforTSP.firstjava;

import java.io.*;
import java.util.Random;

class thanhPho{
    public int toaDox;
    public int toaDoy;
}

public class FirstJava {
    static int totalCities;
    static double[][] khoangCach;    //[start][end] trả về khoảng cách giữa 2 thành phố
    static int[][][] caThe; //[thế hệ][NST][gen] trả về giá trị gen ở vị trí nhất định trong 1 NST của 1 thế hệ

    static String linkFile = "G:\\IdeaProjects\\GAforTSP\\eil51.tsp";  //Để đường dẫn của file chứa thông tin các đỉnh
    static int theHe = 0;
    static int maxTheHe = 10000;
    static int maxCaThe = 100; //tối thiếu là 100
    static double tiLeLaiGhep = 0.5;
    static double tiLeDotBien = 0.5;
    static int startCity = 1; //Thành phố bắt đầu (Sử dụng trong hàm khoiTaoQuanThe_Random())

    static int[] bestCaThe = new int[maxTheHe+5]; //[Thế hệ] Trả về nhiễm sắc thể tốt nhất của thế hệ đó

    static double[][] fitness = new double[maxTheHe+5][maxCaThe+5]; //[Thế hệ][Nhiễm sắc thể] trả về điểm fitness của NST trong thế hệ
    //Options (Sử dụng để debug)
    static boolean print_File = false; //có hay không in ra file .tsp
    static boolean print_toaDo = false; //có hay không in ra tọa độ
    static boolean print_khoangCach = false; //có hay không in ra khoảng cách giữa các thành phố
    static boolean print_first_theHe = false; //Có hay không in ra các NST trong thế hệ đầu tiên
    static boolean print_fitness = false; //Có hay không in ra fitness của 1 NST trong 1 thế hệ
    static boolean print_eFitness = true; //In ra nhiễm sắc thể và điểm fitness tốt nhất trong thế hệ

    //Các giá trị tốt nhất (Sử dụng để ghi lại câu trả lời)
    static int bestNST;
    static int bestTheHe;

    public static double khoangCach(int x1,int y1,int x2,int y2){
        //hàm tính khoảng cách
        double n;
        n = Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
        return n;
    }

    //Các hàm sinh ngẫu nhiên
    public static int genRandom(int gioiHan){
        //gioiHan là giá trị giới hạn, các số được sinh ra nằm trong [1;gioiHan]
        //VD: Nếu muốn sinh ra 1 hoặc 2, hãy đặt gioiHan = 2
        Random output = new Random();
        int number = output.nextInt(gioiHan);
        return number+1;
    }
    public static double genRandomDouble(){
        //Sinh kiểu double, các số được random trong khoảng [0;1)
        Random output = new Random();
        double number = output.nextDouble();
        return number;
    }

    public static void khoiTao() {
        //Khởi tạo cấu trúc dữ liệu và lưu nó trong mảng 2 chiều
        BufferedReader readBuffer = null;
        try{
            readBuffer = new BufferedReader(new FileReader(linkFile));
            String line;
            do{
                //Đọc từng dòng của file đến phần bắt đầu tọa độ
                line = readBuffer.readLine();
                if(line.equals("NODE_COORD_SECTION")) break; //Thoát khỏi vòng lặp nếu gặp NODE_COORD_SECTION
                if(line.contains("DIMENSION")){
                    //Đọc giá trị của tổng số thành phố
                    String[] result = line.split(" : ");
                    totalCities = Integer.parseInt(result[1]);
                }
                if(print_File) System.out.println(line);
            }while (true);
            thanhPho dsThanhPho[] = new thanhPho[totalCities+5];
            khoangCach = new double[totalCities+5][totalCities+5];
            do{
                //Đọc từng dòng của file và lưu dữ liệu tọa độ vào mảng 2 chiều
                line = readBuffer.readLine();
                if(line.equals("EOF")) break; //Thoát khỏi vòng lặp nếu gặp EOF
                String[] result = line.split(" ");
                dsThanhPho[Integer.parseInt(result[0])] = new thanhPho();
                dsThanhPho[Integer.parseInt(result[0])].toaDox = Integer.parseInt(result[1]);
                dsThanhPho[Integer.parseInt(result[0])].toaDoy = Integer.parseInt(result[2]);
                if(print_toaDo) System.out.println(Integer.parseInt(result[0]) + " " + dsThanhPho[Integer.parseInt(result[0])].toaDox + " " + dsThanhPho[Integer.parseInt(result[0])].toaDoy);
                if(print_File) System.out.println(line);
            }while (true);
            for(int i=1;i<52;i++){
                for (int j=1;j<52;j++){
                    double giaTriKhoangCach;
                    giaTriKhoangCach = khoangCach(dsThanhPho[i].toaDox,dsThanhPho[i].toaDoy,dsThanhPho[j].toaDox,dsThanhPho[j].toaDoy);
                    khoangCach[i][j] = giaTriKhoangCach;
                    if(print_khoangCach) System.out.println(i+" "+j+" "+khoangCach[i][j]);
                }
            }
        }catch(Exception e){
            System.out.println(e);
            System.exit(0);
        }
    }
    public static void khoiTaoQuanThe_Random(){
        int nhiemSacThe = 0;
        caThe = new int[maxTheHe][maxCaThe][totalCities+5];

        //Khởi tạo thế hệ đầu tiên
        while (nhiemSacThe!=maxCaThe){
            caThe[theHe][nhiemSacThe][1] = startCity; //Đặt giá trị gen đầu tiên là startCity
            if(print_first_theHe) System.out.print(theHe+"-"+nhiemSacThe+" : "+caThe[theHe][nhiemSacThe][1]);
            //Số thứ tự các gen bắt đầu từ 0 đến totalCities, giá trị bắt đầu từ startCity và kết thúc cũng là startCity
            for(int gene=2;gene<=totalCities;gene++){
                int city = genRandom(totalCities); //random city trong khoảng [1;totalCities]
                //kiểm tra city đã xuất hiện trong NST chưa
                for(int i=1;i<=gene;i++){
                    if(city==caThe[theHe][nhiemSacThe][i] || city==startCity){
                        city = genRandom(totalCities);
                        i=1;
                    }
                }
                caThe[theHe][nhiemSacThe][gene] = city;
                if(print_first_theHe) System.out.print(" "+caThe[theHe][nhiemSacThe][gene]+" ");
            }
            if(print_first_theHe) System.out.print("\n");
            nhiemSacThe++;
        }
    }
    public static void khoiTaoQuanThe_hoanVi(){
        int nhiemSacThe = 0;
        //Khởi tạo cá thể đầu tiên
        caThe = new int[maxTheHe][maxCaThe][totalCities+5];
        for(int i=1;i<=totalCities;i++){
            caThe[theHe][nhiemSacThe][i] = i;
        }
        if(print_first_theHe){
            System.out.print(theHe+"-"+nhiemSacThe+" : ");
            for (int i=1;i<=totalCities;i++){
                System.out.print(" "+caThe[theHe][nhiemSacThe][i]+" ");
            }
            System.out.print("\n");
        }
        nhiemSacThe++;
        //Tiếp tục tạo các cá thể còn lại của thế hệ bằng cách hoán vị gen của cá thể đầu tiên
        while(nhiemSacThe!=maxCaThe){
            System.arraycopy(caThe[theHe][nhiemSacThe-1], 0, caThe[theHe][nhiemSacThe], 0, caThe[theHe][0].length);
            //khởi tạo số lần hoán vị
            int count = genRandom(totalCities/2)+1;
            int p1,p2,temp;
            while (count>0){
                p1 = genRandom(totalCities);
                p2 = genRandom(totalCities);
                while(p1==p2){
                    p2 = genRandom(totalCities);
                }
                temp = caThe[theHe][nhiemSacThe][p1];
                caThe[theHe][nhiemSacThe][p1] = caThe[theHe][nhiemSacThe][p2];
                caThe[theHe][nhiemSacThe][p2] = temp;
                count--;
            }
            if(print_first_theHe){
                System.out.print(theHe+"-"+nhiemSacThe+" : ");
                for (int i=1;i<=totalCities;i++){
                    System.out.print(" "+caThe[theHe][nhiemSacThe][i]+" ");
                }
                System.out.print("\n");
            }
            nhiemSacThe++;
        }
    }

    public static void danhGiaCaThe(){
        //Đánh giá các cá thể và tính điểm fitness
        double totalDist = 0;
        double fitnessValue = 0;
        int cityA,cityB;
        int nhiemSacThe = 0;
        int eNhiemSacThe = 0;
        double eFitness = 0;
        while (nhiemSacThe!=maxCaThe){
            for(int gene=1;gene<totalCities;gene++){
                cityA = caThe[theHe][nhiemSacThe][gene];
                cityB = caThe[theHe][nhiemSacThe][gene+1];
                totalDist += khoangCach[cityA][cityB];
            }
            //Điểm fitness được tính theo công thức fitnessValue = 1/(tổng khoảng cách)
            fitnessValue = 1/totalDist;
            //Lưu giá trị fitness, eFitness
            fitness[theHe][nhiemSacThe] = fitnessValue;
            if(fitnessValue>eFitness){
                eFitness = fitnessValue;
                eNhiemSacThe = nhiemSacThe;
            }
            if(print_fitness) System.out.println(theHe+"-"+nhiemSacThe+": "+fitnessValue);
            nhiemSacThe++;
            totalDist = 0;
        }
        if(print_eFitness) System.out.println("[Tốt nhất] "+theHe+"-"+eNhiemSacThe+" : "+eFitness);
        //Lưu giá trị NST tốt nhất trong thế hệ
        bestCaThe[theHe] = eNhiemSacThe;
        if(theHe==maxTheHe-1){ //Do thế hệ được tính từ 0 nên chúng ta chỉ xét đến maxTheHe-1
            System.out.println("\nFinal Results:");
            for (int i=0;i<maxTheHe;i++){
                for(int j=0;j<maxCaThe;j++){
                    if(fitness[i][j]>fitness[bestTheHe][bestNST]){
                        bestTheHe = i;
                        bestNST =j;
                    }
                }
            }
            System.out.print(bestTheHe+"-"+bestNST+" : C: ");
            for(int gene=1;gene<=totalCities;gene++){
                System.out.print(" "+caThe[bestTheHe][bestNST][gene]+" ");
                if(gene < totalCities){
                    cityA = caThe[bestTheHe][bestNST][gene];
                    cityB = caThe[bestTheHe][bestNST][gene+1];
                    totalDist += khoangCach[cityA][cityB];
                }
            }
            System.out.print(" | D: "+totalDist+" | F: "+fitness[bestTheHe][bestNST]);
        }
    }

    public static int[] laiGhep(int[] cha,int[] me){
        int[] child = new int[totalCities+5];
        int p = genRandom(totalCities);
        System.arraycopy(cha, 1, child, 1, p); //Cắt ở vị trí p của NST cha và đưa vào NST con
        int contro = p+1;
        vonglap1:
        for(int i=1;i<=totalCities;i++){
            for(int j=1;j<=p;j++){ //Sàng lọc những gen trong NST mẹ chưa có trong NST con để thêm vào NST con
                if(me[i]==child[j]) continue vonglap1;
            }
            child[contro] = me[i];
            contro++;
        }
        return child;
    }
    public static int luaChonChaMe(){
        //Áp dụng Tournament Selection (Đọc thêm tại: https://www.tutorialspoint.com/genetic_algorithms/genetic_algorithms_parent_selection.htm)
        int[] daRandom = new int[10]; //Mảng để lưu các giá trị đã random tránh trùng lặp
        int temp,dem=0;
        int parent; //parent sẽ là cá thể có điểm fitness tốt nhất trong 10 cá thể random
        Vonglap1:
        while (true){
            //Random ra 10 NST khác nhau (Việc này yêu cầu thế hệ phải có ít nhất 10 cá thể)
            temp = genRandom(maxCaThe)-1;
            for(int i=0;i<dem;i++){
                if(temp==daRandom[i]) continue Vonglap1;
            }
            daRandom[dem]=temp;
            dem++;
            if(dem==10) break;
        }
//        for (int n: daRandom) System.out.println(n);
        parent = daRandom[0];
        for(int i=1;i<10;i++){
            if(fitness[theHe][parent]<fitness[theHe][daRandom[i]]){
                parent=daRandom[i];
            }
        }
//        System.out.println(parent);
        return parent;
    }
    public static void theHeTiepTheo(){
        int parentA,parentB;
        int badFitness;

        System.arraycopy(caThe[theHe-1], 0, caThe[theHe], 0, maxCaThe);
        //Lai ghép để tạo quần thể mới
        for(int soLanLaiGhep=0;soLanLaiGhep<maxCaThe*tiLeLaiGhep;soLanLaiGhep++){
            parentA = luaChonChaMe();
            parentB = luaChonChaMe();
            while (parentB == parentA) parentB = luaChonChaMe();

            int[] child = laiGhep(caThe[theHe-1][parentA],caThe[theHe-1][parentB]);

            badFitness=0;
            for(int i=1;i<maxCaThe;i++){
                if(fitness[theHe-1][badFitness]>fitness[theHe-1][i]){
                    badFitness=i;
                }
            }
            System.arraycopy(child, 1, caThe[theHe][badFitness], 1, totalCities);
        }
        //Tạo đột biến
        for(int i=0;i<maxCaThe;i++){
            double rand = genRandomDouble();
            if(rand<tiLeDotBien){
                int count = genRandom(totalCities/20);
                int p1,p2,temp;
                while (count>0){
                    p1 = genRandom(totalCities);
                    p2 = genRandom(totalCities);
                    while(p1==p2){
                        p2 = genRandom(totalCities);
                    }
                    temp = caThe[theHe][i][p1];
                    caThe[theHe][i][p1] = caThe[theHe][i][p2];
                    caThe[theHe][i][p2] = temp;
                    count--;
                }
            }
        }

    }
    public static void main(String[] args){
        System.out.println("Chương trình đã chạy!");
        khoiTao(); //Đọc file và khởi tạo cấu trúc dữ liệu
        //Khởi tạo Quần thể thế hệ đầu tiên. Có 2 cách là hoán vị và random, thực hiện 1 trong 2.
//        khoiTaoQuanThe_Random();
        khoiTaoQuanThe_hoanVi();
        danhGiaCaThe();
        theHe++;
        while (theHe!=maxTheHe){
            theHeTiepTheo();
            danhGiaCaThe();
            theHe++;
        }
    }
}
