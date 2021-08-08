package co.uk.mattelliot;


import org.apache.commons.lang3.StringUtils;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.util.LinkedList;

public class LoadingPapers {
    public static void main(String[] args) {
        String s1 = "Computer_science_paper_2__HL.pdf";
        String s2 = "Computer_science_paper_2__HL_markscheme.pdf";
        String s3 = "9700_s04_ms.pdf";
        String s4 = "9700_s04_qp_1.pdf";

        String s5 = "9700_w04_ms_6.pdf";
        String s6 = "9700_w04_qp_6.pdf";

        System.out.println(StringUtils.difference(s1, s2));
        System.out.println(StringUtils.difference(s3, s4));
        System.out.println(StringUtils.difference(s5, s6));


        DiffMatchPatch dmp = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Diff> diff = dmp.diffMain(s5, s6, true);
        System.out.println(diff.get(1).getClass());




    }
}
