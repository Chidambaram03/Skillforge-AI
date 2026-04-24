package com.skillforge.app.service;

import com.skillforge.app.model.Question;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Master 17-Topic Parameterized Question Engine.
 * shuffle() returns String[] so all build() calls are type-safe.
 */
@Service
public class RandomizedQuestionEngine {

    private final Random rng = new Random();

    public Question generate(String topic, String level) {
        switch (topic) {
            case "Percentage":                  return percentage(level);
            case "Profit and Loss":             return profitLoss(level);
            case "Time and Work":               return timeWork(level);
            case "Time Speed Distance":         return timeSpeedDistance(level);
            case "Ratio and Proportion":        return ratio(level);
            case "Simple and Compound Interest":return interest(level);
            case "Averages":                    return averages(level);
            case "Mixtures and Allegations":    return mixtures(level);
            case "Permutation":                 return permutation(level);
            case "Combination":                 return combination(level);
            case "Probability":                 return probability(level);
            case "Number Systems":              return numberSystems(level);
            case "Logical Series":              return logicalSeries(level);
            case "Coding Decoding":             return codingDecoding(level);
            case "Blood Relations":             return bloodRelations(level);
            case "Direction Sense":             return directionSense(level);
            case "Data Interpretation":         return dataInterpretation(level);
            default:                            return percentage(level);
        }
    }

    public Question generateMathQuestion(String level) {
        String[] topics = {"Percentage","Profit and Loss","Time and Work",
            "Time Speed Distance","Ratio and Proportion","Averages",
            "Probability","Number Systems","Logical Series","Blood Relations"};
        return generate(topics[rng.nextInt(topics.length)], level);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private int p(int lo, int hi) { return rng.nextInt(hi - lo + 1) + lo; }

    /** Returns shuffled options as String[4]. */
    private String[] sh(int a, int b, int c, int d) {
        int[] arr = {a, b, c, d};
        for (int i = 3; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = arr[i]; arr[i] = arr[j]; arr[j] = t;
        }
        return new String[]{String.valueOf(arr[0]),String.valueOf(arr[1]),
                            String.valueOf(arr[2]),String.valueOf(arr[3])};
    }

    private String slot(String[] o, int val) {
        String v = String.valueOf(val);
        if (o[0].equals(v)) return "A";
        if (o[1].equals(v)) return "B";
        if (o[2].equals(v)) return "C";
        return "D";
    }

    private Question make(String topic, String level,
                           String q, String[] o, int correct, String exp) {
        Question qn = new Question();
        qn.setTopic(topic); qn.setDifficultyLevel(level); qn.setQuestionText(q);
        qn.setOptionA(o[0]); qn.setOptionB(o[1]); qn.setOptionC(o[2]); qn.setOptionD(o[3]);
        qn.setCorrectOption(slot(o, correct));
        qn.setExplanation(exp);
        qn.setQuestionHash(hash(q));
        return qn;
    }

    /** For questions whose options are already Strings (fractions, words etc.) */
    private Question makeS(String topic, String level,
                            String q, String oA, String oB, String oC, String oD,
                            String correct, String exp) {
        Question qn = new Question();
        qn.setTopic(topic); qn.setDifficultyLevel(level); qn.setQuestionText(q);
        qn.setOptionA(oA); qn.setOptionB(oB); qn.setOptionC(oC); qn.setOptionD(oD);
        qn.setCorrectOption(correct); qn.setExplanation(exp);
        qn.setQuestionHash(hash(q));
        return qn;
    }

    private String hash(String s) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                        .digest(s.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, h).toString(16).substring(0, 16);
        } catch (Exception e) { return String.valueOf(s.hashCode()); }
    }

    private int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    private int lcm(int a, int b) { return a / gcd(a, b) * b; }
    private int fact(int n) { int f=1; for(int i=2;i<=n;i++) f*=i; return f; }
    private int nPr(int n, int r) { return fact(n)/fact(n-r); }
    private int nCr(int n, int r) { if(r>n) return 0; return fact(n)/(fact(r)*fact(n-r)); }

    // ── 1. Percentage ─────────────────────────────────────────────────────────

    private Question percentage(String level) {
        switch (level) {
          case "L1": {
            int val=p(100,1000), pct=p(5,50), ans=val*pct/100;
            String[] o=sh(ans,ans+5,ans-10,ans+15);
            return make("Percentage",level,"What is "+pct+"% of "+val+"?",o,ans,
                pct+"% of "+val+" = ("+pct+"/100) x "+val+" = "+ans);
          }
          case "L2": {
            int orig=p(200,2000), pct=p(5,40), inc=orig+orig*pct/100;
            String[] o=sh(inc,inc+20,inc-15,inc+5);
            return make("Percentage",level,"A price of Rs."+orig+" is increased by "+pct+"%. New price?",o,inc,
                "New price = "+orig+" + "+orig+"x"+pct+"/100 = Rs."+inc);
          }
          case "L3": {
            int base=p(100,500), r1=p(10,30), r2=p(5,25);
            int ans=(int)Math.round(base*(1+r1/100.0)*(1-r2/100.0));
            String[] o=sh(ans,ans+10,ans-8,ans+20);
            return make("Percentage",level,"Rs."+base+" increased by "+r1+"% then decreased by "+r2+"%. Final?",o,ans,
                "After +"+r1+"%: "+base+"x"+(1+r1/100.0)+". After -"+r2+"%: approx Rs."+ans);
          }
          default: {
            int sal=p(500,3000), d1=p(10,25), d2=p(5,15);
            double eff=100-d1-d2+(d1*d2/100.0);
            int net=(int)Math.round(sal*eff/100);
            String[] o=sh(net,net+30,net-25,net+50);
            return make("Percentage",level,"Successive discounts "+d1+"% and "+d2+"% on Rs."+sal+". Net SP?",o,net,
                "Effective discount="+d1+"+"+d2+"-("+d1+"x"+d2+"/100). SP=Rs."+net);
          }
        }
    }

    // ── 2. Profit and Loss ────────────────────────────────────────────────────

    private Question profitLoss(String level) {
        switch (level) {
          case "L1": {
            int cp=p(100,1000), pct=p(5,40), sp=cp+cp*pct/100;
            String[] o=sh(sp,sp+20,sp-15,sp+5);
            return make("Profit and Loss",level,"CP=Rs."+cp+", profit "+pct+"%. Find SP.",o,sp,
                "SP=CP+Profit="+cp+"+"+(cp*pct/100)+"=Rs."+sp);
          }
          case "L2": {
            int sp=p(200,2000), pct=p(10,35), cp=sp*100/(100+pct);
            String[] o=sh(cp,cp+30,cp-20,cp+10);
            return make("Profit and Loss",level,"SP=Rs."+sp+" at "+pct+"% profit. Find CP.",o,cp,
                "CP=SP x 100/(100+P%)="+sp+" x 100/"+( 100+pct)+"=Rs."+cp);
          }
          case "L3": {
            int mp=p(500,2000), disc=p(5,20), prof=p(5,20);
            int sp=mp-mp*disc/100, cp=sp*100/(100+prof);
            String[] o=sh(cp,cp+40,cp-30,cp+15);
            return make("Profit and Loss",level,"MP=Rs."+mp+", discount "+disc+"%, profit "+prof+"%. Find CP.",o,cp,
                "SP="+sp+". CP=SP/(1+p%)=Rs."+cp);
          }
          default: {
            int cp1=p(100,500), cp2=p(100,500), p1=p(10,30), l2=p(5,20);
            int sp1=cp1+cp1*p1/100, sp2=cp2-cp2*l2/100;
            int net=(sp1+sp2)-(cp1+cp2);
            String dir=net>=0?"profit":"loss";
            int abs=Math.abs(net);
            String[] o=sh(abs,abs+25,abs-18,abs+10);
            return make("Profit and Loss",level,"A(CP="+cp1+") sold at "+p1+"% profit; B(CP="+cp2+") at "+l2+"% loss. Net "+dir+"?",o,abs,
                "SP(A)="+sp1+", SP(B)="+sp2+". Net="+net+" ("+dir+")");
          }
        }
    }

    // ── 3. Time and Work ──────────────────────────────────────────────────────

    private Question timeWork(String level) {
        switch (level) {
          case "L1": {
            int days=p(4,20);
            String[] o=sh(days,days+2,days-1,days+5);
            return make("Time and Work",level,"A finishes work in "+days+" days alone. Days needed?",o,days,
                "A does 1/"+days+" work/day. Total = "+days+" days.");
          }
          case "L2": {
            int a=p(6,20), b=p(4,15);
            int lc=lcm(a,b), c=lc/(lc/a+lc/b);
            String[] o=sh(c,c+2,c-1,c+3);
            return make("Time and Work",level,"A in "+a+" days, B in "+b+" days. Together in?",o,c,
                "Rate=1/"+a+"+1/"+b+". Days="+c);
          }
          case "L3": {
            int a=p(10,20), b=p(8,18), d=p(2,5);
            double rem=1-(double)d/a;
            int bd=(int)Math.ceil(rem*b);
            String[] o=sh(bd,bd+2,bd-1,bd+4);
            return make("Time and Work",level,"A("+a+" days), B("+b+" days). A works "+d+" days, B finishes. B's days?",o,bd,
                "A does "+d+"/"+a+". Remaining="+String.format("%.2f",rem)+". B needs "+bd+" days.");
          }
          default: {
            int f=p(10,30), e=p(15,40);
            int lc=lcm(f,e), net=lc/f-lc/e;
            int c=net>0?lc/net:f*e;
            String[] o=sh(c,c+5,c-3,c+8);
            return make("Time and Work",level,"Pipe fills tank in "+f+" hrs, outlet empties in "+e+" hrs. Both open fill time?",o,c,
                "Net rate=1/"+f+"-1/"+e+". Time="+c+" hrs.");
          }
        }
    }

    // ── 4. Time Speed Distance ────────────────────────────────────────────────

    private Question timeSpeedDistance(String level) {
        switch (level) {
          case "L1": {
            int s=p(20,100), t=p(1,8), d=s*t;
            String[] o=sh(d,d+10,d-5,d+20);
            return make("Time Speed Distance",level,"Speed="+s+"km/h, time="+t+"h. Distance?",o,d,"D=SxT="+s+"x"+t+"="+d+"km");
          }
          case "L2": {
            int d=p(100,500), t1=p(2,6), t2=p(2,6);
            int s1=d/t1, s2=d/t2, avg=2*s1*s2/(s1+s2);
            String[] o=sh(avg,avg+5,avg-3,avg+8);
            return make("Time Speed Distance",level,"Goes "+d+"km at "+s1+", returns at "+s2+". Avg speed?",o,avg,
                "Avg=2s1s2/(s1+s2)="+avg+"km/h");
          }
          case "L3": {
            int len=p(100,400), sp=p(36,90);
            int sec=(int)Math.round(len/(sp*5.0/18));
            String[] o=sh(sec,sec+2,sec-1,sec+4);
            return make("Time Speed Distance",level,"Train "+len+"m long at "+sp+"km/h. Time to cross pole (sec)?",o,sec,
                "Speed="+sp+"x5/18 m/s. Time="+len+"/speed=~"+sec+"s");
          }
          default: {
            int s1=p(40,100), s2=p(30,90), d=p(100,500);
            int rel=s1+s2, tim=d*60/rel;
            String[] o=sh(tim,tim+5,tim-3,tim+10);
            return make("Time Speed Distance",level,"Trains "+s1+" and "+s2+" km/h, "+d+"km apart. Meet time(min)?",o,tim,
                "Relative speed="+rel+"km/h. Time="+d+"/"+rel+"hr="+tim+"min");
          }
        }
    }

    // ── 5. Ratio and Proportion ───────────────────────────────────────────────

    private Question ratio(String level) {
        int a=p(2,8), b=p(3,9);
        int total=(a+b)*p(10,50), shareA=total*a/(a+b);
        switch (level) {
          case "L1": case "L2": {
            String[] o=sh(shareA,shareA+10,shareA-5,total-shareA);
            return make("Ratio and Proportion",level,"Rs."+total+" divided "+a+":"+b+". First share?",o,shareA,
                "Share="+a+"/("+a+"+"+b+")xRs."+total+"=Rs."+shareA);
          }
          default: {
            int c=p(2,6), d=p(3,8), num=a*c, den=b*d;
            int tot2=(num+den)*p(5,20), share=tot2*num/(num+den);
            String[] o=sh(share,share+15,share-10,share+25);
            return make("Ratio and Proportion",level,"Compound ratio "+a+":"+b+" and "+c+":"+d+" divides Rs."+tot2+". First part?",o,share,
                "Compound="+num+":"+den+". Part="+num+"/"+(num+den)+"xRs."+tot2+"=Rs."+share);
          }
        }
    }

    // ── 6. Simple and Compound Interest ──────────────────────────────────────

    private Question interest(String level) {
        int pp=p(1000,10000), r=p(5,15), t=p(1,5);
        switch (level) {
          case "L1": {
            int si=pp*r*t/100;
            String[] o=sh(si,si+50,si-30,si+100);
            return make("Simple and Compound Interest",level,"SI on Rs."+pp+" at "+r+"% for "+t+" year(s)?",o,si,"SI=PRT/100="+si);
          }
          case "L2": {
            int ci=(int)Math.round(pp*Math.pow(1+r/100.0,t)-pp);
            String[] o=sh(ci,ci+40,ci-30,ci+80);
            return make("Simple and Compound Interest",level,"CI on Rs."+pp+" at "+r+"% for "+t+" year(s)?",o,ci,
                "CI=P[(1+r/100)^t-1]=Rs."+ci);
          }
          case "L3": {
            int si2=pp*r*2/100;
            int ci2=(int)Math.round(pp*Math.pow(1+r/100.0,2)-pp);
            int diff=Math.abs(ci2-si2);
            String[] o=sh(diff,diff+10,diff-5,diff+20);
            return make("Simple and Compound Interest",level,"Difference CI-SI on Rs."+pp+" at "+r+"% for 2 years?",o,diff,
                "Diff=P(r/100)^2=Rs."+diff);
          }
          default: {
            int target=p(5000,20000), r2=p(4,12), t2=p(2,4);
            int pr=(int)(target/Math.pow(1+r2/100.0,t2));
            String[] o=sh(pr,pr+200,pr-150,pr+400);
            return make("Simple and Compound Interest",level,"Principal for Rs."+target+" after "+t2+" yrs at "+r2+"% CI?",o,pr,
                "P=A/(1+r/100)^t=Rs."+pr);
          }
        }
    }

    // ── 7. Averages ───────────────────────────────────────────────────────────

    private Question averages(String level) {
        switch (level) {
          case "L1": {
            int n=p(3,6); int sum=0;
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<n;i++){int v=p(10,100);sum+=v;sb.append(v);if(i<n-1)sb.append(", ");}
            int avg=sum/n;
            String[] o=sh(avg,avg+5,avg-3,avg+8);
            return make("Averages",level,"Average of: "+sb+"?",o,avg,"Sum="+sum+". Avg="+sum+"/"+n+"="+avg);
          }
          case "L2": {
            int n=p(4,8), avg=p(30,80), added=p(10,100);
            int nAvg=(avg*n+added)/(n+1);
            String[] o=sh(nAvg,nAvg+2,nAvg-1,nAvg+4);
            return make("Averages",level,"Avg of "+n+" numbers="+avg+". "+added+" added. New avg?",o,nAvg,
                "New sum="+avg+"x"+n+"+"+added+"="+(avg*n+added)+". Avg="+(avg*n+added)+"/"+(n+1)+"="+nAvg);
          }
          case "L3": {
            int n=p(5,10), af=p(30,80), k=p(2,n-1), ar=p(20,70);
            int sf=af*n, sr=ar*(n-k), sk=sf-sr, ak=sk/k;
            String[] o=sh(ak,ak+3,ak-2,ak+7);
            return make("Averages",level,"Avg of "+n+"="+af+". Avg of remaining "+(n-k)+"="+ar+". Avg of excluded "+k+"?",o,ak,
                "Excluded sum="+sk+". Avg="+sk+"/"+k+"="+ak);
          }
          default: {
            int ac=p(12,16), n=p(20,40), ra=p(25,50), na=p(11,15);
            int nav=(ac*n-ra+na)/n;
            String[] o=sh(nav,nav+1,nav-1,nav+2);
            return make("Averages",level,"Avg age "+n+" students="+ac+". Teacher("+ra+") leaves, student("+na+") joins. New avg?",o,nav,
                "Sum="+ac+"x"+n+"-"+ra+"+"+na+"="+(ac*n-ra+na)+". Avg="+nav);
          }
        }
    }

    // ── 8. Mixtures and Allegations ───────────────────────────────────────────

    private Question mixtures(String level) {
        int p1=p(20,70), p2=p(p1+10,90), tgt=p(p1+1,p2-1);
        int rA=p2-tgt, rB=tgt-p1;
        switch (level) {
          case "L1": case "L2": {
            String[] o=sh(rA,rA+1,rB,rA+2);
            return make("Mixtures and Allegations",level,"Mix "+p1+"% and "+p2+"% to get "+tgt+"%. Ratio first:second?",o,rA,
                "Allegation: ("+p2+"-"+tgt+"):("+tgt+"-"+p1+")="+rA+":"+rB);
          }
          default: {
            int cap=p(40,200), drawn=p(5,cap/4), rds=p(2,3);
            int pure=(int)Math.round(cap*Math.pow((double)(cap-drawn)/cap,rds));
            String[] o=sh(pure,pure+5,pure-4,pure+10);
            return make("Mixtures and Allegations",level,cap+"L vessel. "+drawn+"L replaced "+rds+" times. Pure milk left?",o,pure,
                "Pure="+cap+"x(("+cap+"-"+drawn+")/"+cap+")^"+rds+"="+pure+"L");
          }
        }
    }

    // ── 9. Permutation ────────────────────────────────────────────────────────

    private Question permutation(String level) {
        int n=p(4,7), r=p(2,Math.min(n,4)), npr=nPr(n,r);
        switch (level) {
          case "L1": case "L2": {
            String[] o=sh(npr,npr+10,npr/2,npr*2);
            return make("Permutation",level,"P("+n+","+r+") — arrange "+r+" from "+n+" distinct items?",o,npr,"P(n,r)=n!/(n-r)!="+npr);
          }
          default: {
            int lt=p(4,6), rep=p(2,3), arr=fact(lt)/fact(rep);
            String[] o=sh(arr,arr+10,fact(lt),arr+fact(rep)*2);
            return make("Permutation",level,"Distinct "+lt+"-letter arrangements with "+rep+" repeated letters?",o,arr,
                "Arrangements="+lt+"!/"+rep+"!="+fact(lt)+"/"+fact(rep)+"="+arr);
          }
        }
    }

    // ── 10. Combination ───────────────────────────────────────────────────────

    private Question combination(String level) {
        int n=p(5,10), r=p(2,Math.min(n,4)), ncr=nCr(n,r);
        switch (level) {
          case "L1": case "L2": {
            String[] o=sh(ncr,ncr+5,nPr(n,r),ncr+10);
            return make("Combination",level,"Committee of "+r+" from "+n+" people. Ways?",o,ncr,"C("+n+","+r+")="+ncr);
          }
          default: {
            int boys=p(5,8), girls=p(3,6), sz=p(3,5), mg=p(1,2);
            int tot=0;
            for(int g=mg;g<=Math.min(girls,sz);g++) tot+=nCr(girls,g)*nCr(boys,sz-g);
            String[] o=sh(tot,tot+10,nCr(boys+girls,sz),Math.max(0,tot-5));
            return make("Combination",level,"From "+boys+" boys, "+girls+" girls, choose "+sz+" with >= "+mg+" girl(s). Ways?",o,tot,
                "Sum across valid girl counts="+tot);
          }
        }
    }

    // ── 11. Probability ───────────────────────────────────────────────────────

    private Question probability(String level) {
        switch (level) {
          case "L1": {
            int tot=p(5,20), fav=p(1,tot-1), g=gcd(fav,tot);
            String frac=(fav/g)+"/"+(tot/g);
            return makeS("Probability",level,"Bag: "+tot+" balls, "+fav+" red. P(red)?",
                frac,(tot-fav)/g+"/"+(tot/g),(fav/g+1)+"/"+(tot/g),"1/"+(tot/g+1),"A",
                "P="+fav+"/"+tot+"="+frac);
          }
          case "L2": {
            return makeS("Probability",level,"Two dice rolled. P(sum=12)?",
                "1/36","2/36","3/36","5/36","A","Only (6,6) gives 12. P=1/36");
          }
          case "L3": {
            int r=p(3,6), b=p(2,5), tot=r+b;
            int w=nCr(r,2), all=nCr(tot,2), g=gcd(w,all);
            String frac=w/g+"/"+(all/g);
            return makeS("Probability",level,"Bag: "+r+" red, "+b+" blue. P(both drawn red)?",
                frac,(w+1)/g+"/"+(all/g),"1/"+b,w+"/"+r,"A",
                "P=C("+r+",2)/C("+tot+",2)="+w+"/"+all+"="+frac);
          }
          default: {
            return makeS("Probability",level,"Card from 52. P(heart OR ace)?",
                "4/13","17/52","5/13","1/4","A","P=13/52+4/52-1/52=16/52=4/13");
          }
        }
    }

    // ── 12. Number Systems ────────────────────────────────────────────────────

    private Question numberSystems(String level) {
        switch (level) {
          case "L1": {
            int n=p(10,99), d=p(2,9), rem=n%d;
            String[] o=sh(rem,(rem+1)%d,(rem+2)%d,Math.max(0,d-1));
            return make("Number Systems",level,"Remainder when "+n+" divided by "+d+"?",o,rem,n+" / "+d+" = "+(n/d)+" rem "+rem);
          }
          case "L2": {
            int a=p(10,50), b=p(5,30);
            int l=lcm(a,b), h=gcd(a,b);
            String[] o=sh(l,l+h,Math.max(1,l-h),l*2);
            return make("Number Systems",level,"LCM of "+a+" and "+b+"?",o,l,"GCD="+h+". LCM="+a+"x"+b+"/"+h+"="+l);
          }
          case "L3": {
            int base=p(2,9), exp=p(10,30);
            int e=exp%4==0?4:exp%4;
            int ud=(int)Math.pow(base,e)%10;
            String[] o=sh(ud,(ud+2)%10,(ud+4)%10,(ud+6)%10);
            return make("Number Systems",level,"Unit digit of "+base+"^"+exp+"?",o,ud,"Cyclicity: "+base+"^"+e+" ends in "+ud);
          }
          default: {
            int n=p(100,999), d=p(7,13), rem=(n%d);
            int sq=(rem*rem)%d;
            String[] o=sh(sq,(sq+1)%d,(sq+d-1)%d,(sq+2)%d);
            return make("Number Systems",level,"Remainder of "+n+"^2 divided by "+d+"?",o,sq,
                n+" mod "+d+"="+rem+". "+rem+"^2 mod "+d+"="+sq);
          }
        }
    }

    // ── 13. Logical Series ────────────────────────────────────────────────────

    private Question logicalSeries(String level) {
        switch (level) {
          case "L1": {
            int st=p(1,20), step=p(2,10);
            int[] s={st,st+step,st+2*step,st+3*step,st+4*step};
            String[] o=sh(s[4],s[4]+step+1,s[4]-step,s[4]+step*2);
            return make("Logical Series",level,"Next: "+s[0]+", "+s[1]+", "+s[2]+", "+s[3]+", ?",o,s[4],
                "Arithmetic +"+step+". Next="+s[3]+"+"+step+"="+s[4]);
          }
          case "L2": {
            int st=p(2,5), f=p(2,3);
            int[] s=new int[5]; s[0]=st;
            for(int i=1;i<5;i++) s[i]=s[i-1]*f;
            String[] o=sh(s[4],s[4]+f,s[3],s[4]*f);
            return make("Logical Series",level,"Next: "+s[0]+", "+s[1]+", "+s[2]+", "+s[3]+", ?",o,s[4],
                "Geometric x"+f+". Next="+s[3]+"x"+f+"="+s[4]);
          }
          case "L3": {
            int a=p(3,8), b=p(2,6);
            int[] s={a,b,a+b,a+2*b,2*a+3*b};
            String[] o=sh(s[4],s[4]+1,s[4]-2,s[3]+s[4]);
            return make("Logical Series",level,"Pattern: "+s[0]+", "+s[1]+", "+s[2]+", "+s[3]+", ?",o,s[4],
                "Each term adds previous two. Next="+s[4]);
          }
          default: {
            int a=p(5,15);
            int[] s={a,a*2,a*2-1,(a*2-1)*2,(a*2-1)*2-1};
            String[] o=sh(s[4],s[4]+2,s[4]-3,s[4]+5);
            return make("Logical Series",level,"Next: "+s[0]+", "+s[1]+", "+s[2]+", "+s[3]+", ?",o,s[4],
                "Pattern: x2, -1, x2, -1. Next="+s[3]+"-1="+s[4]);
          }
        }
    }

    // ── 14. Coding Decoding ───────────────────────────────────────────────────

    private Question codingDecoding(String level) {
        String[] words={"BOOK","CARE","DARK","EARN","FACE"};
        String word=words[rng.nextInt(words.length)];
        int shift=p(2,5);
        String coded=shiftEnc(word,shift);
        switch (level) {
          case "L1": case "L2": {
            String w2=word.charAt(0)+"ATE";
            return makeS("Coding Decoding",level,"If '"+word+"'='"+coded+"', code for '"+w2+"'?",
                shiftEnc(w2,shift),shiftEnc(w2,shift-1),shiftEnc(word.charAt(0)+"EAT",shift),shiftEnc(word.charAt(0)+"TAN",shift),"A",
                "Each letter +"+shift+" in alphabet.");
          }
          default: {
            String w2="SMART", c2=rev(w2);
            String unk=rev("BRAVE");
            return makeS("Coding Decoding",level,"Code: '"+w2+"'='"+c2+"'. Decode '"+unk+"'?",
                "BRAVE","EVARB","RBVAE","VEARB","A","Reverse the coded string.");
          }
        }
    }

    private String shiftEnc(String w, int sh) {
        StringBuilder sb=new StringBuilder();
        for(char c:w.toCharArray()) sb.append((char)(((c-'A'+sh)%26)+'A'));
        return sb.toString();
    }
    private String rev(String s) { return new StringBuilder(s).reverse().toString(); }

    // ── 15. Blood Relations ───────────────────────────────────────────────────

    private Question bloodRelations(String level) {
        switch (level) {
          case "L1":
            return makeS("Blood Relations",level,"A is B's sister. B is C's brother. C is D's son. A related to D?",
                "Daughter","Son","Niece","Sister","A","A is D's daughter.");
          case "L2":
            return makeS("Blood Relations",level,"'She is only daughter of my father's son.' Relation to Raj?",
                "Daughter","Niece","Sister","Cousin","A","Father's son=Raj. She is Raj's daughter.");
          case "L3":
            return makeS("Blood Relations",level,"P is Q's brother; R is Q's mother; S is R's father; T is S's mother. P related to T?",
                "Great Grandson","Grandson","Son","Nephew","A","T->S->R->Q->P. P is T's great grandson.");
          default:
            return makeS("Blood Relations",level,"A+B: A father of B. A-B: A wife of B. If P+Q-R, how is P related to R?",
                "Father-in-law","Father","Uncle","Brother","A","P is father of Q. Q is wife of R. So P is R's father-in-law.");
        }
    }

    // ── 16. Direction Sense ───────────────────────────────────────────────────

    private Question directionSense(String level) {
        int d=p(3,15), d2=p(3,12);
        switch (level) {
          case "L1":
            return makeS("Direction Sense",level,"Ravi walks "+d+"km North, turns right "+d2+"km. Direction from start?",
                "North-East","North-West","South-East","East","A","North then East = North-East.");
          case "L2": {
            int dist=(int)Math.round(Math.sqrt(d*d+d2*d2));
            return makeS("Direction Sense",level,"A: "+d+"km East, "+d2+"km North. Straight-line distance?",
                dist+"km",(dist+2)+"km",(dist-1)+"km",(dist+3)+"km","A",
                "Pythagoras: sqrt("+d+"^2+"+d2+"^2)=~"+dist+"km");
          }
          case "L3":
            return makeS("Direction Sense",level,"Meera faces West, turns 90 degrees clockwise. She now faces?",
                "North","South","East","West","A","West + 90 clockwise = North.");
          default:
            return makeS("Direction Sense",level,"At 6AM shadow falls to West. Man faces shadow. After 90 left turn, faces?",
                "South","North","East","West","A","Faces West (shadow). Left 90 = South.");
        }
    }

    // ── 17. Data Interpretation ───────────────────────────────────────────────

    private Question dataInterpretation(String level) {
        int[] sales={p(100,500),p(200,600),p(150,550),p(300,700),p(250,650)};
        String[] yrs={"2019","2020","2021","2022","2023"};
        int mx=sales[0], mi=0;
        for(int i=1;i<5;i++) if(sales[i]>mx){mx=sales[i];mi=i;}
        switch (level) {
          case "L1": case "L2": {
            String[] o=sh(mx,mx+50,Math.max(0,mx-30),mx+20);
            return make("Data Interpretation",level,
                "Sales: "+yrs[0]+":"+sales[0]+", "+yrs[1]+":"+sales[1]+", "+yrs[2]+":"+sales[2]+", "+yrs[3]+":"+sales[3]+", "+yrs[4]+":"+sales[4]+". Max units?",
                o,mx,"Maximum in "+yrs[mi]+" = "+mx+" units.");
          }
          default: {
            int pc=(int)(((double)(sales[4]-sales[0])/sales[0])*100);
            String dir=pc>=0?"increase":"decrease";
            int apc=Math.abs(pc);
            return makeS("Data Interpretation",level,"Sales "+yrs[0]+"="+sales[0]+", "+yrs[4]+"="+sales[4]+". % "+dir+"?",
                apc+"%",(apc+5)+"%",Math.max(0,apc-3)+"%",(apc+10)+"%","A",
                "% change=(("+sales[4]+"-"+sales[0]+")/"+sales[0]+")*100="+pc+"%");
          }
        }
    }
}
