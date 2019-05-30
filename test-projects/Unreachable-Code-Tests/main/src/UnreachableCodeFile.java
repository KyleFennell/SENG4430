public class UnreachableCodeFile
{
    public UnreachableCodeFile()
    {
        int test = 0;
        if(false){}
        do{}while(false);

        if(false && true && true ){}
        do{}while(false && test > 0);

        if(false || false){}
        do{}while(false && false || true && false);

        if(1 > 1){}
        do{}while(1 > 1 || 0 > 1);

        if(0 >= 1){}
        do{}while(0 >= 1);

        if(1 > 1){}
        do{}while(1 > 1);

        if(1 >= 2){}
        do{}while(1 >= 2);
    }
}