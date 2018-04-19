
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User_Len
 */
public class code_writer {
        private RandomAccessFile raf;
    private int jumpFlag;
    public code_writer(){ 
        jumpFlag = 0;
    }
    //informs the code writer that the translation of a new VM file has started
    public void setFileName(String fileName){
        try{
            raf = new RandomAccessFile(fileName,"rw");
        }catch(IOException e){}
    }
    //writes the assembly code that is the translation of the given arithmetic command
    public void writeArithmetic(String command){
        try{
            switch(command){
                case "add":{
                    raf.writeBytes(arithmeticFormat + "M=M+D\n");
                    break;
                }
                case "sub":{
                    raf.writeBytes(arithmeticFormat + "M=M-D\n");
                    break;
                }
                case "and":{
                    raf.writeBytes(arithmeticFormat + "M=M&D\n");
                    break;
                }
                case "or":{
                    raf.writeBytes(arithmeticFormat + "M=M|D\n");
                    break;
                }
                case "gt":{
                    raf.writeBytes(booleanFormat("JGT"));//not <=
                    jumpFlag++;
                    break;
                }
                case "lt":{
                    raf.writeBytes(booleanFormat("JLT"));//not >=
                    jumpFlag++;
                    break;
                }
                case "eq":{
                    raf.writeBytes(booleanFormat("JEQ"));//not !=
                    jumpFlag++;
                    break;
                }
                case "not":{
                    raf.writeBytes("@SP\nA=M-1\nM=!M\n");
                    break;
                }
                case "neg":{
                    raf.writeBytes("D=0\n@SP\nA=M-1\nM=D-M\n");
                    break;
                }
            }
        }catch(IOException e){}
    }
    //writes the assembly code that is the translation of the given command
    public void writePushPop(String command, String segment, int index){
        try{
            String abbreviation = "";
        switch(segment){
            case"local":{
                abbreviation = "LCL";
                break;
            }case"constant":{
                raf.writeBytes("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
                break;
            }case"argument":{
                abbreviation = "ARG";
                break;
            }case"this":{
                abbreviation = "THIS";
                break;
            }case"that":{
                abbreviation = "THAT";
                break;
            }case"pointer":{
                if(index == 0){
                abbreviation = "THIS";
                }else{
                abbreviation = "THAT";
                }
                break;
            }case"temp":{
                abbreviation = "R5";
                break;
            }case"static":{
                abbreviation = Integer.toString(16+index);
                break;
            }
        }
        if(!segment.equals("constant")){
            if(command.equals("push")){
                if(segment.equals("temp")){
                    raf.writeBytes(pushTemplate1(abbreviation, index+5, false));
                }else if(segment.equals("pointer")){
                    raf.writeBytes(pushTemplate1(abbreviation, index, true));
                }else{
                raf.writeBytes(pushTemplate1(abbreviation, index, false));
                }
        }else{
                if(segment.equals("temp")){
                    raf.writeBytes(popTemplate1(abbreviation, index +5,false));
                }else if(segment.equals("pointer")){
                    raf.writeBytes(popTemplate1(abbreviation, index, true));
                }else{
                    raf.writeBytes(popTemplate1(abbreviation, index,false));
                }
                
        }
        }
        
        }catch(IOException e){}
        
    }
    //closes the output file
    public void close()
    {
        try{
            raf.close();
        }catch(IOException e){}
    }
    private String arithmeticFormat = "@SP\n" + "AM=M-1\n" + "D=M\n" + "A=A-1\n";
    private String pushTemplate1(String segment, int index, boolean isDirect){
        //cuando es un puntero, solo lee la data puesta en THIS o THAT
        //cuando es estatico, solo lee la data en la dirrecion
        String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";

        return "@" + segment + "\n" +
                "D=M\n"+
                noPointerCode +
                "@SP\n" +
                "A=M\n" +
                "M=D\n" +
                "@SP\n" +
                "M=M+1\n";    
    }
    private String popTemplate1(String segment, int index, boolean isDirect){  
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

        return "@" + segment + "\n" +
                noPointerCode +
                "@R13\n" +
                "M=D\n" +
                "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@R13\n" +
                "A=M\n" +
                "M=D\n";
    }
      private String booleanFormat(String type){
          return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@FALSE" + jumpFlag + "\n" +
                "D;" + type + "\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=0\n" +
                "@CONTINUE" + jumpFlag + "\n" +
                "0;JMP\n" +
                "(FALSE" + jumpFlag + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=-1\n" +
                "(CONTINUE" + jumpFlag + ")\n";
    }
}
