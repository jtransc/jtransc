/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */

package regexodus;

import regexodus.ds.IntBitSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Term implements REFlags, Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    //runtime Term types
    static final int CHAR = 0;
    static final int BITSET = 1;
    static final int BITSET2 = 2;
    static final int ANY_CHAR = 4;
    static final int ANY_CHAR_NE = 5;

    static final int REG = 6;
    static final int REG_I = 7;
    static final int FIND = 8;
    static final int FINDREG = 9;
    static final int SUCCESS = 10;

    /*optimization-transparent types*/
    static final int BOUNDARY = 11;
    static final int DIRECTION = 12;
    static final int UBOUNDARY = 13;
    static final int UDIRECTION = 14;

    static final int GROUP_IN = 15;
    static final int GROUP_OUT = 16;
    static final int VOID = 17;

    static final int START = 18;
    static final int END = 19;
    static final int END_EOL = 20;
    static final int LINE_START = 21;
    static final int LINE_END = 22;
    static final int LAST_MATCH_END = 23;

    static final int CNT_SET_0 = 24;
    static final int CNT_INC = 25;
    static final int CNT_GT_EQ = 26;
    static final int READ_CNT_LT = 27;

    static final int CRSTORE_CRINC = 28; //store on 'actual' search entry
    static final int CR_SET_0 = 29;
    static final int CR_LT = 30;
    static final int CR_GT_EQ = 31;


    static final int LITERAL_START = 60;
    static final int LITERAL_END = 61;

    /*optimization-nontransparent types*/
    static final int BRANCH = 32;
    static final int BRANCH_STORE_CNT = 33;
    static final int BRANCH_STORE_CNT_AUX1 = 34;

    static final int PLOOKAHEAD_IN = 35;
    static final int PLOOKAHEAD_OUT = 36;
    static final int NLOOKAHEAD_IN = 37;
    static final int NLOOKAHEAD_OUT = 38;
    static final int PLOOKBEHIND_IN = 39;
    static final int PLOOKBEHIND_OUT = 40;
    static final int NLOOKBEHIND_IN = 41;
    static final int NLOOKBEHIND_OUT = 42;
    static final int INDEPENDENT_IN = 43; //functionally the same as NLOOKAHEAD_IN
    static final int INDEPENDENT_OUT = 44;

    static final int REPEAT_0_INF = 45;
    static final int REPEAT_MIN_INF = 46;
    static final int REPEAT_MIN_MAX = 47;
    static final int REPEAT_REG_MIN_INF = 48;
    static final int REPEAT_REG_MIN_MAX = 49;

    static final int BACKTRACK_0 = 50;
    static final int BACKTRACK_MIN = 51;
    static final int BACKTRACK_FIND_MIN = 52;
    static final int BACKTRACK_FINDREG_MIN = 53;
    static final int BACKTRACK_REG_MIN = 54;

    static final int MEMREG_CONDITION = 55;
    static final int LOOKAHEAD_CONDITION_IN = 56;
    static final int LOOKAHEAD_CONDITION_OUT = 57;
    static final int LOOKBEHIND_CONDITION_IN = 58;
    static final int LOOKBEHIND_CONDITION_OUT = 59;

    //optimization
    static final int FIRST_TRANSPARENT = BOUNDARY;
    static final int LAST_TRANSPARENT = CR_GT_EQ;

    // compile-time: length of vars[] (see makeTree())
    private static final int VARS_LENGTH = 4;

    // compile-time variable indices:
    private static final int MEMREG_COUNT = 0;    //refers current memreg index
    private static final int CNTREG_COUNT = 1;   //refers current counters number
    private static final int DEPTH = 2;      //refers current depth: (((depth=3)))
    private static final int LOOKAHEAD_COUNT = 3;    //refers current memreg index

    private static final int LIMITS_LENGTH = 3;
    private static final int LIMITS_PARSE_RESULT_INDEX = 2;
    private static final int LIMITS_OK = 1;
    private static final int LIMITS_FAILURE = 2;

    private static final int LITERAL_FLAG = 64;

    //static CustomParser[] customParsers=new CustomParser[256];

    // **** CONTROL FLOW ****

    // next-to-execute and next-if-failed commands;
    Term next, failNext;

    // **** TYPES ****

    int type = VOID;
    boolean inverse;

    // used with type=CHAR
    char c;

    // used with type=FIND
    int distance;
    boolean eat;

    // used with type=BITSET(2);
    IntBitSet bitset;
    IntBitSet[] bitset2;
    private boolean[] categoryBitset;  //types(unicode categories)
    boolean mode_insensitive;
    boolean mode_reverse;
    boolean mode_bracket;

    // used for optimization with type=BITSET,BITSET2
    int weight;

    // **** MEMORISATION ****

    // memory slot, used with type=REG,GROUP_IN,GROUP_OUT
    int memreg = -1;


    // **** COUNTERS ****

    // max|min number of iterations
    // used with CNT_GT_EQ ,REPEAT_* etc.;
    int minCount, maxCount;

    // used with REPEAT_*,REPEAT_REG_*;
    Term target;

    // a counter slot to increment & compare with maxCount (CNT_INC etc.);
    int cntreg = 0;

    // lookahead group id;
    int lookaheadId;

    // **** COMPILE HELPERS ****

    Term prev;
    Term in;
    Term out;
    Term out1;
    protected Term first;
    Term current;

    //new!!
    Term branchOut;

    //protected  boolean newBranch=false,closed=false;
    //protected  boolean newBranch=false;

    //for debugging
    private static int instances;
    private int instanceNum;

    Term() {
        //for debugging
        instanceNum = instances;
        instances++;
        in = out = this;
    }

    Term(int type) {
        this();
        this.type = type;
    }

    static void makeTree(String s, int[] flags, Pattern re) throws PatternSyntaxException {
        instances = 0;
        char[] data = s.toCharArray();
        makeTree(data, 0, data.length, flags, re);
    }

    private static void makeTree(char[] data, int offset, int end,
                                 int[] flags, Pattern re) throws PatternSyntaxException {
        // memreg,counter,depth,lookahead
        int[] vars = {1, 0, 0, 0}; //don't use counters[0]

        //collect iterators for subsequent optimization
        ArrayList<TermIterator> iterators = new ArrayList<TermIterator>();
        HashMap<String, Integer> groupNames = new HashMap<String, Integer>();

        Pretokenizer t = new Pretokenizer(data, offset, end);
        Term term = makeTree(t, data, vars, flags, new Group(), iterators, groupNames);

        // convert closing outer bracket into success term
        term.out.type = SUCCESS;

        //throw out opening bracket
        Term first = term.next;

        // Optimisation:
        //Term optimized = first;
        //Optimizer opt = Optimizer.find(first);
        //if (opt != null) optimized = opt.makeFirst(first);

        for (TermIterator i : iterators) {
            i.optimize();
        }

        //re.root = optimized;
        re.root = first;
        re.root0 = first;
        re.memregs = vars[MEMREG_COUNT];
        re.counters = vars[CNTREG_COUNT];
        re.lookaheads = vars[LOOKAHEAD_COUNT];
        re.namedGroupMap = groupNames;
    }

    private static Term makeTree(Pretokenizer t, char[] data, int[] vars,
                                 int[] flags, Term term, ArrayList<TermIterator> iterators, HashMap<String, Integer> groupNames) throws PatternSyntaxException {
        if (vars.length != VARS_LENGTH)
            throw new IllegalArgumentException("vars.length should be " + VARS_LENGTH + ", not " + vars.length);
        //Term term=new Term(isMemReg? vars[MEMREG_COUNT]: -1);
        // use memreg 0 as insignificant
        //Term term=new Group(isMemReg? vars[MEMREG_COUNT]: 0);
        while (true) {
            t.next();
            term.append(t.tOffset, t.tOutside, data, vars, flags, iterators, groupNames);
            switch (t.ttype) {
                case Pretokenizer.FLAGS:
                    flags[0] = t.flags(flags[0]);
                    continue;
                case Pretokenizer.CLASS_GROUP:
                    t.next();
                    Term clg = new Term();
                    CharacterClass.parseGroup(data, t.tOffset, t.tOutside, clg,
                            (flags[0] & IGNORE_CASE) > 0, (flags[0] & IGNORE_SPACES) > 0,
                            (flags[0] & UNICODE) > 0, (flags[0] & XML_SCHEMA) > 0);
                    term.append(clg);
                    continue;
                case Pretokenizer.PLAIN_GROUP:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, new int[]{t.flags(flags[0])}, new Group(), iterators, groupNames));
                    break;
                case Pretokenizer.NAMED_GROUP:
                    String gname = t.groupName;
                    int id;
                    if (Character.isDigit(gname.charAt(0))) {
                        try {
                            id = Integer.parseInt(gname);
                        } catch (NumberFormatException e) {
                            throw new PatternSyntaxException("group name starts with digit but is not a number");
                        }
                        if (groupNames.containsValue(id)) {
                            if (t.groupDeclared)
                                throw new PatternSyntaxException("group redeclaration: " + gname + "; use ({=id}...) for multiple group assignments");
                        }
                        if (vars[MEMREG_COUNT] <= id) vars[MEMREG_COUNT] = id + 1;
                    } else {
                        Integer no = groupNames.get(gname);
                        if (no == null) {
                            id = vars[MEMREG_COUNT]++;
                            groupNames.put(t.groupName, id);
                        } else {
                            if (t.groupDeclared)
                                throw new PatternSyntaxException("group redeclaration " + gname + "; use ({=name}...) for group reassignments");
                            id = no;
                        }
                    }
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Group(id), iterators, groupNames));
                    break;
                case '(':
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Group(vars[MEMREG_COUNT]++), iterators, groupNames));
                    break;
                case Pretokenizer.POS_LOOKAHEAD:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Lookahead(vars[LOOKAHEAD_COUNT]++, true), iterators, groupNames));
                    break;
                case Pretokenizer.NEG_LOOKAHEAD:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Lookahead(vars[LOOKAHEAD_COUNT]++, false), iterators, groupNames));
                    break;
                case Pretokenizer.POS_LOOKBEHIND:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Lookbehind(vars[LOOKAHEAD_COUNT]++, true), iterators, groupNames));
                    break;
                case Pretokenizer.NEG_LOOKBEHIND:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new Lookbehind(vars[LOOKAHEAD_COUNT]++, false), iterators, groupNames));
                    break;
                case Pretokenizer.INDEPENDENT_REGEX:
                    vars[DEPTH]++;
                    term.append(makeTree(t, data, vars, flags, new IndependentGroup(vars[LOOKAHEAD_COUNT]++), iterators, groupNames));
                    break;
                case Pretokenizer.CONDITIONAL_GROUP:
                    vars[DEPTH]++;
                    t.next();
                    Term fork;
                    boolean positive = true;
                    switch (t.ttype) {
                        case Pretokenizer.NEG_LOOKAHEAD:
                            positive = false;
                        case Pretokenizer.POS_LOOKAHEAD:
                            vars[DEPTH]++;
                            Lookahead la = new Lookahead(vars[LOOKAHEAD_COUNT]++, positive);
                            makeTree(t, data, vars, flags, la, iterators, groupNames);
                            fork = new ConditionalExpr(la);
                            break;
                        case Pretokenizer.NEG_LOOKBEHIND:
                            positive = false;
                        case Pretokenizer.POS_LOOKBEHIND:
                            vars[DEPTH]++;
                            Lookbehind lb = new Lookbehind(vars[LOOKAHEAD_COUNT]++, positive);
                            makeTree(t, data, vars, flags, lb, iterators, groupNames);
                            fork = new ConditionalExpr(lb);
                            break;
                        case '(':
                            t.next();
                            if (t.ttype != ')') throw new PatternSyntaxException("malformed condition");
                            int memregNo;
                            if (Character.isDigit(data[t.tOffset])) memregNo = makeNumber(t.tOffset, t.tOutside, data);
                            else {
                                String gn = new String(data, t.tOffset, t.tOutside - t.tOffset);
                                Integer gno = groupNames.get(gn);
                                if (gno == null)
                                    throw new PatternSyntaxException("unknown group name in conditional expr.: " + gn);
                                memregNo = gno;
                            }
                            fork = new ConditionalExpr(memregNo);
                            break;
                        default:
                            throw new PatternSyntaxException("malformed conditional expression: " + t.ttype + " '" + (char) t.ttype + "'");
                    }
                    term.append(makeTree(t, data, vars, flags, fork, iterators, groupNames));
                    break;
                case '|':
                    term.newBranch();
                    break;
                case Pretokenizer.END:
                    if (vars[DEPTH] > 0) throw new PatternSyntaxException("unbalanced parenthesis");
                    term.close();
                    return term;
                case ')':
                    if (vars[DEPTH] <= 0) throw new PatternSyntaxException("unbalanced parenthesis");
                    term.close();
                    vars[DEPTH]--;
                    return term;
                case Pretokenizer.COMMENT:
                    while (t.ttype != ')') t.next();
                    continue;
                default:
                    throw new PatternSyntaxException("unknown token type: " + t.ttype);
            }
        }
    }

    private static int makeNumber(int off, int out, char[] data) {
        int n = 0;
        for (int i = off; i < out; i++) {
            int d = data[i] - '0';
            if (d < 0 || d > 9) return -1;
            n *= 10;
            n += d;
        }
        return n;
    }

    private void append(int offset, int end, char[] data,
                        int[] vars, int[] flags, ArrayList<TermIterator> iterators, HashMap<String, Integer> gmap) throws PatternSyntaxException {
        int[] limits = new int[3];
        int i = offset;
        Term tmp, current = this.current;
        while (i < end) {
            char c = data[i];
            boolean greedy = true;
            if((flags[0] & LITERAL_FLAG) != LITERAL_FLAG) {
                switch (c) {
                    //operations
                    case '*':
                        if (current == null) throw new PatternSyntaxException("missing term before *");
                        i++;
                        if (i < end && data[i] == '?') {
                            greedy = false;
                            i++;
                        }
                        tmp = greedy ? makeGreedyStar(vars, current, iterators) :
                                makeLazyStar(vars, current);
                        current = replaceCurrent(tmp);
                        break;

                    case '+':
                        if (current == null) throw new PatternSyntaxException("missing term before +");
                        i++;
                        if (i < end && data[i] == '?') {
                            greedy = false;
                            i++;
                        }
                        tmp = greedy ? makeGreedyPlus(vars, current, iterators) :
                                makeLazyPlus(vars, current);
                        current = replaceCurrent(tmp);
                        break;

                    case '?':
                        if (current == null) throw new PatternSyntaxException("missing term before ?");
                        i++;
                        if (i < end && data[i] == '?') {
                            greedy = false;
                            i++;
                        }

                        tmp = greedy ? makeGreedyQMark(vars, current) :
                                makeLazyQMark(vars, current);
                        current = replaceCurrent(tmp);
                        break;

                    case '{':
                        limits[0] = 0;
                        limits[1] = -1;
                        int le = parseLimits(i + 1, end, data, limits);
                        if (limits[LIMITS_PARSE_RESULT_INDEX] == LIMITS_OK) { //parse ok
                            if (current == null) throw new PatternSyntaxException("missing term before {}");
                            i = le;
                            if (i < end && data[i] == '?') {
                                greedy = false;
                                i++;
                            }
                            tmp = greedy ? makeGreedyLimits(vars, current, limits, iterators) :
                                    makeLazyLimits(vars, current, limits);
                            current = replaceCurrent(tmp);
                            break;
                        } else { //unicode class or named backreference
                            if (data[i + 1] == '\\') { //'{\name}' - backreference
                                int p = i + 2;
                                if (p == end) throw new PatternSyntaxException("'group_id' expected");
                                char cp = data[p];
                                boolean mi = false, mb = false, mr = false;
                                while (Category.Z.contains(cp) || Category.Po.contains(cp)) {
                                    p++;
                                    if (p == end) throw new PatternSyntaxException("'group_id' expected");
                                    switch (cp)
                                    {
                                        case '@': mi = !mi;
                                            break;
                                        case '/': mr = !mr;
                                            break;
                                        case ':': mb = !mb;
                                            break;
                                    }
                                    cp = data[p];
                                }
                                BackReference br = new BackReference(-1, mi || (flags[0] & IGNORE_CASE) > 0, mr, mb);
                                i = parseGroupId(data, p, end, br, gmap);
                                current = append(br);
                                continue;
                            } else {
                                Term t = new Term();
                                i = CharacterClass.parseName(data, i, end, t, false, (flags[0] & IGNORE_SPACES) > 0);
                                current = append(t);
                                continue;
                            }
                        }

                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        if ((flags[0] & IGNORE_SPACES) > 0) {
                            i++;
                            continue;
                        }
                        //else go on as default

                        //symbolic items
                    default:
                        tmp = new Term();
                        i = parseTerm(data, i, end, tmp, flags[0]);

                        if (tmp.type == LITERAL_START) {
                            flags[0] |= LITERAL_FLAG;
                            continue;
                        } else if (tmp.type == LITERAL_END) {
                            flags[0] &= ~LITERAL_FLAG;
                            continue;
                        }

                        if (tmp.type == END && i < end) {
                            throw new PatternSyntaxException("'$' is not a last term in the group: <" + new String(data, offset, end - offset) + ">");
                        }
                        //"\A"
                        //if(tmp.type==START && i>(offset+1)){
                        //   throw new PatternSyntaxException("'^' is not a first term in the group: <"+new String(data,offset,end-offset)+">");
                        //}

                        current = append(tmp);
                        break;
                }
            }
            else {
                tmp = new Term();
                i = parseTerm(data, i, end, tmp, flags[0]);

                if (tmp.type == LITERAL_START) {
                    flags[0] |= LITERAL_FLAG;
                    continue;
                } else if (tmp.type == LITERAL_END) {
                    flags[0] &= ~LITERAL_FLAG;
                    continue;
                }

                if (tmp.type == END && i < end) {
                    throw new PatternSyntaxException("'$' is not a last term in the group: <" + new String(data, offset, end - offset) + ">");
                }

                current = append(tmp);
            }
        }
    }

    /*
    static boolean isIdentifierPart()
    {

    }*/


    private static int parseGroupId(char[] data, int i, int end, Term term, HashMap<String, Integer> gmap) throws PatternSyntaxException {
        int id;
        int nstart = i;
        if (Character.isDigit(data[i])) {
            while (Character.isDigit(data[i])) {
                i++;
                if (i == end) throw new PatternSyntaxException("group_id expected");
            }
            id = makeNumber(nstart, i, data);
        } else {
            while (Category.IdentifierPart.contains(data[i])) {
                i++;
                if (i == end) throw new PatternSyntaxException("group_id expected");
            }
            String s = new String(data, nstart, i - nstart);
            Integer no = gmap.get(s);
            if (no == null) throw new PatternSyntaxException("backreference to unknown group: " + s);
            id = no;
        }
        while (Category.Z.contains(data[i])) {
            i++;
            if (i == end) throw new PatternSyntaxException("'}' expected");
        }

        int c = data[i++];

        if (c != '}') throw new PatternSyntaxException("'}' expected");

        term.memreg = id;
        return i;
    }

    Term append(Term term) throws PatternSyntaxException {
        //Term prev=this.prev;
        Term current = this.current;
        if (current == null) {
            in.next = term;
            term.prev = in;
            this.current = term;
            return term;
        }
        link(current, term);
        //this.prev=current;
        this.current = term;
        return term;
    }

    Term replaceCurrent(Term term) throws PatternSyntaxException {
        //Term prev=this.prev;
        Term prev = current.prev;
        if (prev != null) {
            Term in = this.in;
            if (prev == in) {
                //in.next=term;
                //term.prev=in;
                in.next = term.in;
                term.in.prev = in;
            } else link(prev, term);
        }
        this.current = term;
        return term;
    }


    private void newBranch() throws PatternSyntaxException {
        close();
        startNewBranch();
    }


    void close() throws PatternSyntaxException {
      /*
      Term prev=this.prev;
      if(prev!=null){
         Term current=this.current;
         if(current!=null){
            link(prev,current);
            prev=current;
            this.current=null;
         }
         link(prev,out);
         this.prev=null;
      }
      */
        Term current = this.current;
        if (current != null) linkd(current, out);
        else in.next = out;
    }

    private static void link(Term term, Term next) {
        linkd(term, next.in);
        next.prev = term;
    }

    private static void linkd(Term term, Term next) {
        Term prev_out = term.out;
        if (prev_out != null) {
            prev_out.next = next;
        }
        Term prev_out1 = term.out1;
        if (prev_out1 != null) {
            prev_out1.next = next;
        }
        Term prev_branch = term.branchOut;
        if (prev_branch != null) {
            prev_branch.failNext = next;
        }
    }

    void startNewBranch() throws PatternSyntaxException {
        Term tmp = in.next;
        Term b = new Branch();
        in.next = b;
        b.next = tmp;
        b.in = null;
        b.out = null;
        b.out1 = null;
        b.branchOut = b;
        current = b;
    }

    private static Term makeGreedyStar(int[] vars, Term term, ArrayList<TermIterator> iterators) throws PatternSyntaxException {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case GROUP_IN: {
                Term b = new Branch();
                b.next = term.in;
                term.out.next = b;

                b.in = b;
                b.out = null;
                b.out1 = null;
                b.branchOut = b;

                return b;
            }
            default: {
                return new TermIterator(term, 0, -1, iterators);
            }
        }
    }

    private static Term makeLazyStar(int[] vars, Term term) {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case GROUP_IN: {
                Term b = new Branch();
                b.failNext = term.in;
                term.out.next = b;

                b.in = b;
                b.out = b;
                b.out1 = null;
                b.branchOut = null;

                return b;
            }
            default: {
                Term b = new Branch();
                b.failNext = term;
                term.next = b;

                b.in = b;
                b.out = b;
                b.out1 = null;
                b.branchOut = null;

                return b;
            }
        }
    }

    private static Term makeGreedyPlus(int[] vars, Term term, ArrayList<TermIterator> iterators) throws PatternSyntaxException {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case INDEPENDENT_IN://?
            case GROUP_IN: {
                Term b = new Branch();
                b.next = term.in;
                term.out.next = b;

                b.in = term.in;
                b.out = null;
                b.out1 = null;
                b.branchOut = b;


                return b;
            }
            default: {
                return new TermIterator(term, 1, -1, iterators);
            }
        }
    }

    private static Term makeLazyPlus(int[] vars, Term term) {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case GROUP_IN: {
                Term b = new Branch();
                term.out.next = b;
                b.failNext = term.in;

                b.in = term.in;
                b.out = b;
                b.out1 = null;
                b.branchOut = null;

                return b;
            }
            case REG:
            default: {
                Term b = new Branch();
                term.next = b;
                b.failNext = term;

                b.in = term;
                b.out = b;
                b.out1 = null;
                b.branchOut = null;

                return b;
            }
        }
    }

    private static Term makeGreedyQMark(int[] vars, Term term) {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case GROUP_IN: {
                Term b = new Branch();
                b.next = term.in;

                b.in = b;
                b.out = term.out;
                b.out1 = null;
                b.branchOut = b;

                return b;
            }
            case REG:
            default: {
                Term b = new Branch();
                b.next = term;

                b.in = b;
                b.out = term;
                b.out1 = null;
                b.branchOut = b;

                return b;
            }
        }
    }

    private static Term makeLazyQMark(int[] vars, Term term) {
        //vars[STACK_SIZE]++;
        switch (term.type) {
            case GROUP_IN: {
                Term b = new Branch();
                b.failNext = term.in;

                b.in = b;
                b.out = b;
                b.out1 = term.out;
                b.branchOut = null;

                return b;
            }
            case REG:
            default: {
                Term b = new Branch();
                b.failNext = term;

                b.in = b;
                b.out = b;
                b.out1 = term;
                b.branchOut = null;

                return b;
            }
        }
    }

    private static Term makeGreedyLimits(int[] vars, Term term, int[] limits, ArrayList<TermIterator> iterators) throws PatternSyntaxException {
        //vars[STACK_SIZE]++;
        int m = limits[0];
        int n = limits[1];
        switch (term.type) {
            case GROUP_IN: {
                int cntreg = vars[CNTREG_COUNT]++;
                Term reset = new Term(CR_SET_0);
                reset.cntreg = cntreg;
                Term b = new Term(BRANCH);

                Term inc = new Term(CRSTORE_CRINC);
                inc.cntreg = cntreg;

                reset.next = b;

                if (n >= 0) {
                    Term lt = new Term(CR_LT);
                    lt.cntreg = cntreg;
                    lt.maxCount = n;
                    b.next = lt;
                    lt.next = term.in;
                } else {
                    b.next = term.in;
                }
                term.out.next = inc;
                inc.next = b;

                if (m >= 0) {
                    Term gt = new Term(CR_GT_EQ);
                    gt.cntreg = cntreg;
                    gt.maxCount = m;
                    b.failNext = gt;

                    reset.in = reset;
                    reset.out = gt;
                    reset.out1 = null;
                    reset.branchOut = null;
                } else {
                    reset.in = reset;
                    reset.out = null;
                    reset.out1 = null;
                    reset.branchOut = b;
                }
                return reset;
            }
            default: {
                return new TermIterator(term, limits[0], limits[1], iterators);
            }
        }
    }

    private static Term makeLazyLimits(int[] vars, Term term, int[] limits) {
        //vars[STACK_SIZE]++;
        int m = limits[0];
        int n = limits[1];
        switch (term.type) {
            case GROUP_IN: {
                int cntreg = vars[CNTREG_COUNT]++;
                Term reset = new Term(CR_SET_0);
                reset.cntreg = cntreg;
                Term b = new Term(BRANCH);
                Term inc = new Term(CRSTORE_CRINC);
                inc.cntreg = cntreg;

                reset.next = b;

                if (n >= 0) {
                    Term lt = new Term(CR_LT);
                    lt.cntreg = cntreg;
                    lt.maxCount = n;
                    b.failNext = lt;
                    lt.next = term.in;
                } else {
                    b.failNext = term.in;
                }
                term.out.next = inc;
                inc.next = b;

                if (m >= 0) {
                    Term gt = new Term(CR_GT_EQ);
                    gt.cntreg = cntreg;
                    gt.maxCount = m;
                    b.next = gt;

                    reset.in = reset;
                    reset.out = gt;
                    reset.out1 = null;
                    reset.branchOut = null;

                    return reset;
                } else {
                    reset.in = reset;
                    reset.out = b;
                    reset.out1 = null;
                    reset.branchOut = null;

                    return reset;
                }
            }
            case REG:
            default: {
                Term reset = new Term(CNT_SET_0);
                Term b = new Branch(BRANCH_STORE_CNT);
                Term inc = new Term(CNT_INC);

                reset.next = b;

                if (n >= 0) {
                    Term lt = new Term(READ_CNT_LT);
                    lt.maxCount = n;
                    b.failNext = lt;
                    lt.next = term;
                    term.next = inc;
                    inc.next = b;
                } else {
                    b.next = term;
                    term.next = inc;
                    inc.next = term;
                }

                if (m >= 0) {
                    Term gt = new Term(CNT_GT_EQ);
                    gt.maxCount = m;
                    b.next = gt;

                    reset.in = reset;
                    reset.out = gt;
                    reset.out1 = null;
                    reset.branchOut = null;

                    return reset;
                } else {
                    reset.in = reset;
                    reset.out = b;
                    reset.out1 = null;
                    reset.branchOut = null;

                    return reset;
                }
            }
        }
    }


    private int parseTerm(char[] data, int i, int out, Term term,
                          int flags) throws PatternSyntaxException {
        char c = data[i++];
        boolean inv = false;
        if((flags & LITERAL_FLAG) == LITERAL_FLAG)
        {
            switch (c)
            {
                case '\\':
                    if(i < out + 1 && data[i] == 'E')
                    {
                        term.type = LITERAL_END;
                        return i + 1;
                    }
                default:
                    term.type = CHAR;
                    if ((flags & IGNORE_CASE) == 0) {
                        term.c = c;
                    } else {
                        term.c = Category.caseFold(c);
                    }
                    return i;
            }
        }
        switch (c) {
            case '[':
                return CharacterClass.parseClass(data, i, out, term, (flags & IGNORE_CASE) > 0, (flags & IGNORE_SPACES) > 0, (flags & UNICODE) > 0, (flags & XML_SCHEMA) > 0);

            case '.':
                term.type = (flags & DOTALL) > 0 ? ANY_CHAR : ANY_CHAR_NE;
                break;

            case '$':
                //term.type=mods[MULTILINE_IND]? LINE_END: END; //??
                term.type = (flags & MULTILINE) > 0 ? LINE_END : END_EOL;
                break;

            case '^':
                term.type = (flags & MULTILINE) > 0 ? LINE_START : START;
                break;

            case '\\':
                if (i >= out) throw new PatternSyntaxException("Escape without a character");
                c = data[i++];
                switch (c) {
                    case 'f':
                        c = '\f'; // form feed
                        break;

                    case 'n':
                        c = '\n'; // new line
                        break;

                    case 'r':
                        c = '\r'; // carriage return
                        break;



                    case 't':
                        c = '\t'; // tab
                        break;

                    case 'u':
                        if(i < out - 3)
                            c = (char) ((CharacterClass.toHexDigit(data[i++]) << 12) +
                                (CharacterClass.toHexDigit(data[i++]) << 8) +
                                (CharacterClass.toHexDigit(data[i++]) << 4) +
                                CharacterClass.toHexDigit(data[i++]));
                        else {
                            c = '\0';
                            i = out;
                        }
                        break;

                    case 'x': {   // hex 2-digit number -> char
                        int hex = 0;
                        char d;
                        if ((d = data[i++]) == '{') {
                            while (i < out && (d = data[i++]) != '}') {
                                hex = (hex << 4) + CharacterClass.toHexDigit(d);
                                if (hex > 0xffff || i == out)
                                    throw new PatternSyntaxException("\\x{<out of range or incomplete>}");
                            }
                        } else {
                            hex = (CharacterClass.toHexDigit(d) << 4) +
                                    CharacterClass.toHexDigit(data[i++]);
                        }
                        c = (char) hex;
                        break;
                    }
                    case '0':
                    case 'o':   // oct arbitrary-digit number -> char
                        int oct = 0;
                        for (; i < out; ) {
                            char d = data[i++];
                            if (d >= '0' && d <= '7') {
                                oct *= 8;
                                oct += d - '0';
                                if (oct > 0xffff) {
                                    oct -= d - '0';
                                    oct /= 8;
                                    break;
                                }
                            } else break;
                        }
                        c = (char) oct;
                        break;

                    case 'm':   // decimal number -> char
                        int dec = 0;
                        for (; i < out; ) {
                            char d = data[i++];
                            if (d >= '0' && d <= '9') {
                                dec *= 10;
                                dec += d - '0';
                                if (dec > 0xffff){
                                    dec -= d - '0';
                                    dec /= 10;
                                    break;
                                }
                            } else break;
                        }
                        c = (char) dec;
                        break;

                    case 'c':   // ctrl-char
                        c = (char) (data[i++] & 0x1f);
                        break;

                    case 'D':   // non-digit
                        inv = true;
                        // go on
                    case 'd':   // digit
                        CharacterClass.makeDigit(term, inv, (flags & UNICODE) > 0);
                        return i;

                    case 'S':   // non-space
                        inv = true;
                        // go on
                    case 's':   // space
                        CharacterClass.makeSpace(term, inv, (flags & UNICODE) > 0);
                        return i;

                    case 'W':   // non-letter
                        inv = true;
                        // go on
                    case 'w':   // letter
                        CharacterClass.makeWordChar(term, inv, (flags & UNICODE) > 0);
                        return i;

                    case 'H':
                        inv = true;
                    case 'h':
                        CharacterClass.makeHSpace(term, inv, (flags & UNICODE) > 0);
                        return  i;

                    case 'V':
                        inv = true;
                    case 'v':
                        CharacterClass.makeVSpace(term, inv, (flags & UNICODE) > 0);
                        return  i;

                    case 'B':   // non-(word boundary)
                        inv = true;
                        // go on
                    case 'b':   // word boundary
                        CharacterClass.makeWordBoundary(term, inv, (flags & UNICODE) > 0);
                        return i;

                    case '<':   // word start
                        CharacterClass.makeWordStart(term, (flags & UNICODE) > 0);
                        return i;

                    case '>':   // word end
                        CharacterClass.makeWordEnd(term, (flags & UNICODE) > 0);
                        return i;

                    case 'A':   // text beginning
                        term.type = START;
                        return i;

                    case 'Z':   // text end
                        term.type = END_EOL;
                        return i;

                    case 'z':   // text end
                        term.type = END;
                        return i;

                    case 'G':   // end of last match
                        term.type = LAST_MATCH_END;
                        return i;

                    case 'P':   // \\P{..}
                        inv = true;
                    case 'p':   // \\p{..}
                        i = CharacterClass.parseName(data, i, out, term, inv, (flags & IGNORE_SPACES) > 0);
                        return i;
                    case 'Q':
                        term.type = LITERAL_START;
                        return i;


                    default:
                        if (c >= '1' && c <= '9') {
                            int n = c - '0';
                            while ((i < out) && (c = data[i]) >= '0' && c <= '9') {
                                n = (n * 10) + c - '0';
                                i++;
                            }
                            term.type = (flags & IGNORE_CASE) > 0 ? REG_I : REG;
                            term.memreg = n;
                            return i;
                        }
                  /*
                  if(c<256){
                     CustomParser termp=customParsers[c];
                     if(termp!=null){
                        i=termp.parse(i,data,term);
                        return i;
                     }
                  }
                  */
                }
                term.type = CHAR;
                term.c = c;
                break;

            default:
                if ((flags & IGNORE_CASE) == 0) {
                    term.type = CHAR;
                    term.c = c;
                } else {
                    term.type = CHAR;
                    term.c = Category.caseFold(c);
                    //CharacterClass.makeICase(term, c);
                }
                break;
        }
        return i;
    }


    // one of {n},{n,},{,n},{n1,n2}
    private static int parseLimits(int i, int end, char[] data, int[] limits) throws PatternSyntaxException {
        if (limits.length != LIMITS_LENGTH)
            throw new IllegalArgumentException("limits.length=" + limits.length + ", should be " + LIMITS_LENGTH);
        limits[LIMITS_PARSE_RESULT_INDEX] = LIMITS_OK;
        int ind = 0;
        int v = 0;
        char c;
        while (i < end) {
            c = data[i++];
            switch (c) {
                case ' ':
                    continue;

                case ',':
                    if (ind > 0) throw new PatternSyntaxException("illegal construction: {.. , , ..}");
                    limits[ind++] = v;
                    v = -1;
                    continue;

                case '}':
                    limits[ind] = v;
                    if (ind == 0) limits[1] = v;
                    return i;

                default:
                    if (c > '9' || c < '0') {
                        //throw new PatternSyntaxException("illegal symbol in iterator: '{"+c+"}'");
                        limits[LIMITS_PARSE_RESULT_INDEX] = LIMITS_FAILURE;
                        return i;
                    }
                    if (v < 0) v = 0;
                    v = v * 10 + (c - '0');
            }
        }
        throw new PatternSyntaxException("malformed quantifier");
    }
    static String termLookup(int t)
    {
        switch (t)
        {
            case CHAR: return "CHAR";
            case BITSET: return "BITSET";
            case BITSET2: return "BITSET2";
            case ANY_CHAR: return "ANY_CHAR";
            case ANY_CHAR_NE: return "ANY_CHAR_NE";
            case REG: return "REG";
            case REG_I: return "REG_I";
            case FIND: return "FIND";
            case FINDREG: return "FINDREG";
            case SUCCESS: return "SUCCESS";
            case BOUNDARY: return "BOUNDARY";
            case DIRECTION: return "DIRECTION";
            case UBOUNDARY: return "UBOUNDARY";
            case UDIRECTION: return "UDIRECTION";
            case GROUP_IN: return "GROUP_IN";
            case GROUP_OUT: return "GROUP_OUT";
            case VOID: return "VOID";
            case START: return "START";
            case END: return "END";
            case END_EOL: return "END_EOL";
            case LINE_START: return "LINE_START";
            case LINE_END: return "LINE_END";
            case LAST_MATCH_END: return "LAST_MATCH_END";
            case CNT_SET_0: return "CNT_SET_0";
            case CNT_INC: return "CNT_INC";
            case CNT_GT_EQ: return "CNT_GT_EQ";
            case READ_CNT_LT: return "READ_CNT_LT";
            case CRSTORE_CRINC: return "CRSTORE_CRINC";
            case CR_SET_0: return "CR_SET_0";
            case CR_LT: return "CR_LT";
            case CR_GT_EQ: return "CR_GT_EQ";
            case BRANCH: return "BRANCH";
            case BRANCH_STORE_CNT: return "BRANCH_STORE_CNT";
            case BRANCH_STORE_CNT_AUX1: return "BRANCH_STORE_CNT_AUX1";
            case PLOOKAHEAD_IN: return "PLOOKAHEAD_IN";
            case PLOOKAHEAD_OUT: return "PLOOKAHEAD_OUT";
            case NLOOKAHEAD_IN: return "NLOOKAHEAD_IN";
            case NLOOKAHEAD_OUT: return "NLOOKAHEAD_OUT";
            case PLOOKBEHIND_IN: return "PLOOKBEHIND_IN";
            case PLOOKBEHIND_OUT: return "PLOOKBEHIND_OUT";
            case NLOOKBEHIND_IN: return "NLOOKBEHIND_IN";
            case NLOOKBEHIND_OUT: return "NLOOKBEHIND_OUT";
            case INDEPENDENT_IN: return "INDEPENDENT_IN";
            case INDEPENDENT_OUT: return "INDEPENDENT_OUT";
            case REPEAT_0_INF: return "REPEAT_0_INF";
            case REPEAT_MIN_INF: return "REPEAT_MIN_INF";
            case REPEAT_MIN_MAX: return "REPEAT_MIN_MAX";
            case REPEAT_REG_MIN_INF: return "REPEAT_REG_MIN_INF";
            case REPEAT_REG_MIN_MAX: return "REPEAT_REG_MIN_MAX";
            case BACKTRACK_0: return "BACKTRACK_0";
            case BACKTRACK_MIN: return "BACKTRACK_MIN";
            case BACKTRACK_FIND_MIN: return "BACKTRACK_FIND_MIN";
            case BACKTRACK_FINDREG_MIN: return "BACKTRACK_FINDREG_MIN";
            case BACKTRACK_REG_MIN: return "BACKTRACK_REG_MIN";
            case MEMREG_CONDITION: return "MEMREG_CONDITION";
            case LOOKAHEAD_CONDITION_IN: return "LOOKAHEAD_CONDITION_IN";
            case LOOKAHEAD_CONDITION_OUT: return "LOOKAHEAD_CONDITION_OUT";
            case LOOKBEHIND_CONDITION_IN: return "LOOKBEHIND_CONDITION_IN";
            case LOOKBEHIND_CONDITION_OUT: return "LOOKBEHIND_CONDITION_OUT";
            default: return "UNKNOWN_TERM";
        }
    }
    public String toString() {
        StringBuilder b = new StringBuilder(100);
        //b.append(hashCode());
        b.append(instanceNum);
        b.append(' ');
        b.append(termLookup(type));
        b.append(": ");
        if (inverse) b.append('^');
        switch (type) {
            case VOID:
                b.append("[]");
                b.append(" , ");
                break;
            case CHAR:
                b.append(CharacterClass.stringValue(c));
                b.append(" , ");
                break;
            case ANY_CHAR:
                b.append("dotall, ");
                break;
            case ANY_CHAR_NE:
                b.append("dot-eols, ");
                break;
            case BITSET:
                b.append('[');
                b.append(CharacterClass.stringValue0(bitset));
                b.append(']');
                b.append(" , weight=");
                b.append(weight);
                b.append(" , ");
                break;
            case BITSET2:
                b.append('[');
                b.append(CharacterClass.stringValue2(bitset2));
                b.append(']');
                b.append(" , weight2=");
                b.append(weight);
                b.append(" , ");
                break;
            case START:
                b.append("abs.start");
                break;
            case END:
                b.append("abs.end");
                break;
            case END_EOL:
                b.append("abs.end-eol");
                break;
            case LINE_START:
                b.append("line start");
                break;
            case LINE_END:
                b.append("line end");
                break;
            case LAST_MATCH_END:
                if (inverse) b.append("non-");
                b.append("BOUNDARY");
                break;
            case BOUNDARY:
                if (inverse) b.append("non-");
                b.append("BOUNDARY");
                break;
            case UBOUNDARY:
                if (inverse) b.append("non-");
                b.append("UBOUNDARY");
                break;
            case DIRECTION:
                b.append("DIRECTION");
                break;
            case UDIRECTION:
                b.append("UDIRECTION");
                break;
            case FINDREG:
                b.append('%');
            case FIND:
                b.append(">>>{");
                b.append(target);
                b.append("}, <<");
                b.append(distance);
                if (eat) {
                    b.append(",eat");
                }
                b.append(", ");
                break;
            case REPEAT_0_INF:
                b.append("rpt{");
                b.append(target);
                b.append(",0,inf}");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case REPEAT_MIN_INF:
                b.append("rpt{");
                b.append(target);
                b.append(",");
                b.append(minCount);
                b.append(",inf}");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case REPEAT_MIN_MAX:
                b.append("rpt{");
                b.append(target);
                b.append(",");
                b.append(minCount);
                b.append(",");
                b.append(maxCount);
                b.append("}");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case REPEAT_REG_MIN_INF:
                b.append("rpt{$");
                b.append(memreg);
                b.append(',');
                b.append(minCount);
                b.append(",inf}");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case REPEAT_REG_MIN_MAX:
                b.append("rpt{$");
                b.append(memreg);
                b.append(',');
                b.append(minCount);
                b.append(',');
                b.append(maxCount);
                b.append("}");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case BACKTRACK_0:
                b.append("back(0)");
                break;
            case BACKTRACK_MIN:
                b.append("back(");
                b.append(minCount);
                b.append(")");
                break;
            case BACKTRACK_REG_MIN:
                b.append("back");
                b.append("_$");
                b.append(memreg);
                b.append("(");
                b.append(minCount);
                b.append(")");
                break;
            case GROUP_IN:
                b.append('(');
                if (memreg > 0) b.append(memreg);
                b.append('-');
                b.append(" , ");
                break;
            case GROUP_OUT:
                b.append('-');
                if (memreg > 0) b.append(memreg);
                b.append(')');
                b.append(" , ");
                break;
            case PLOOKAHEAD_IN:
                b.append('(');
                b.append("=");
                b.append(lookaheadId);
                b.append(" , ");
                break;
            case PLOOKAHEAD_OUT:
                b.append('=');
                b.append(lookaheadId);
                b.append(')');
                b.append(" , ");
                break;
            case NLOOKAHEAD_IN:
                b.append("(!");
                b.append(lookaheadId);
                b.append(" , ");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case NLOOKAHEAD_OUT:
                b.append('!');
                b.append(lookaheadId);
                b.append(')');
                b.append(" , ");
                break;
            case PLOOKBEHIND_IN:
                b.append('(');
                b.append("<=");
                b.append(lookaheadId);
                b.append(" , dist=");
                b.append(distance);
                b.append(" , ");
                break;
            case PLOOKBEHIND_OUT:
                b.append("<=");
                b.append(lookaheadId);
                b.append(')');
                b.append(" , ");
                break;
            case NLOOKBEHIND_IN:
                b.append("(<!");
                b.append(lookaheadId);
                b.append(" , dist=");
                b.append(distance);
                b.append(" , ");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case NLOOKBEHIND_OUT:
                b.append("<!");
                b.append(lookaheadId);
                b.append(')');
                b.append(" , ");
                break;
            case MEMREG_CONDITION:
                b.append("(reg");
                b.append(memreg);
                b.append("?)");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case LOOKAHEAD_CONDITION_IN:
                b.append("(cond");
                b.append(lookaheadId);
                b.append(((Lookahead) this).isPositive ? '=' : '!');
                b.append(" , ");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case LOOKAHEAD_CONDITION_OUT:
                b.append("cond");
                b.append(lookaheadId);
                b.append(")");
                if (failNext != null) {
                    b.append(", =>");
                    b.append(failNext.instanceNum);
                    b.append(", ");
                }
                break;
            case REG:
                b.append("$");
                b.append(memreg);
                b.append(", ");
                break;
            case SUCCESS:
                b.append("END");
                break;
            case BRANCH_STORE_CNT_AUX1:
                b.append("(aux1)");
            case BRANCH_STORE_CNT:
                b.append("(cnt)");
            case BRANCH:
                b.append("=>");
                if (failNext != null) b.append(failNext.instanceNum);
                else b.append("null");
                b.append(" , ");
                break;
            default:
                b.append('[');
                switch (type) {
                    case CNT_SET_0:
                        b.append("cnt=0");
                        break;
                    case CNT_INC:
                        b.append("cnt++");
                        break;
                    case CNT_GT_EQ:
                        b.append("cnt>=").append(maxCount);
                        break;
                    case READ_CNT_LT:
                        b.append("->cnt<").append(maxCount);
                        break;
                    case CRSTORE_CRINC:
                        b.append("M(").append(memreg).append(")->,Cr(").append(cntreg).append(")->,Cr(").append(cntreg).append(")++");
                        break;
                    case CR_SET_0:
                        b.append("Cr(").append(cntreg).append(")=0");
                        break;
                    case CR_LT:
                        b.append("Cr(").append(cntreg).append(")<").append(maxCount);
                        break;
                    case CR_GT_EQ:
                        b.append("Cr(").append(cntreg).append(")>=").append(maxCount);
                        break;
                    default:
                        b.append("unknown type: ").append(type);
                }
                b.append("] , ");
        }
        if (next != null) {
            b.append("->");
            b.append(next.instanceNum);
            b.append(", ");
        }
        //b.append("\r\n");
        return b.toString();
    }

    public String toStringAll() {
        return toStringAll(new ArrayList<Integer>());
    }

    private String toStringAll(ArrayList<Integer> v) {
        v.add(instanceNum);
        String s = toString();
        if (next != null) {
            if (!v.contains(next.instanceNum)) {
                s += "\r\n";
                s += next.toStringAll(v);
            }
        }
        if (failNext != null) {
            if (!v.contains(failNext.instanceNum)) {
                s += "\r\n";
                s += failNext.toStringAll(v);
            }
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        if (type != term.type) return false;
        if (inverse != term.inverse) return false;
        if (c != term.c) return false;
        if (distance != term.distance) return false;
        if (eat != term.eat) return false;
        if (weight != term.weight) return false;
        if (memreg != term.memreg) return false;
        if (minCount != term.minCount) return false;
        if (maxCount != term.maxCount) return false;
        if (cntreg != term.cntreg) return false;
        if (lookaheadId != term.lookaheadId) return false;
        if (next != null ? !next.equals(term.next) : term.next != null) return false;
        if (bitset != null ? !bitset.equals(term.bitset) : term.bitset != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(bitset2, term.bitset2) && Arrays.equals(categoryBitset, term.categoryBitset);
//if (!Arrays.equals(brackets, term.brackets)) return false;
        /*
        if (failNext != null ? !failNext.equals(term.failNext) : term.failNext != null) return false;
        if (target != null ? !target.equals(term.target) : term.target != null) return false;
        if (prev != null ? !prev.equals(term.prev) : term.prev != null) return false;
        if (in != null ? !in.equals(term.in) : term.in != null) return false;
        if (out != null ? !out.equals(term.out) : term.out != null) return false;
        if (out1 != null ? !out1.equals(term.out1) : term.out1 != null) return false;
        if (first != null ? !first.equals(term.first) : term.first != null) return false;
        if (current != null ? !current.equals(term.current) : term.current != null) return false;
        return branchOut != null ? branchOut.equals(term.branchOut) : term.branchOut == null;
        */
    }

    @Override
    public int hashCode() {
        int result = next != null ? next.hashCode() : 0;
        result = 31 * result + type;
        result = 31 * result + (inverse ? 1 : 0);
        result = 31 * result + (int) c;
        result = 31 * result + distance;
        result = 31 * result + (eat ? 1 : 0);
        result = 31 * result + (bitset != null ? bitset.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(bitset2);
        result = 31 * result + Arrays.hashCode(categoryBitset);
        result = 31 * result + weight;
        result = 31 * result + memreg;
        result = 31 * result + minCount;
        result = 31 * result + maxCount;
        result = 31 * result + cntreg;
        result = 31 * result + lookaheadId;
        /*
        result = 31 * result + (failNext != null ? failNext.hashCode() : 0);
        result = 31 * result + (target != null ? (this == target ? 73 : target.hashCode()) : 0);
        result = 31 * result + (prev != null ? (this == prev ? 73 : prev.hashCode()) : 0);
        result = 31 * result + (in != null ? (this == in ? 73 : in.hashCode()) : 0);
        result = 31 * result + (out != null ? (this == out ? 73 : out.hashCode()) : 0);
        result = 31 * result + (out1 != null ? (this == out1 ? 73 : out1.hashCode()) : 0);
        result = 31 * result + (first != null ? (this == first ? 73 : first.hashCode()) : 0);
        result = 31 * result + (current != null ? (this == current ? 73 : current.hashCode()) : 0);
        result = 31 * result + (branchOut != null ? (this == branchOut ? 73 : branchOut.hashCode()) : 0);
        */
        return result;
    }
}

class Pretokenizer implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    private static final int START = 1;
    static final int END = 2;
    static final int PLAIN_GROUP = 3;
    static final int POS_LOOKAHEAD = 4;
    static final int NEG_LOOKAHEAD = 5;
    static final int POS_LOOKBEHIND = 6;
    static final int NEG_LOOKBEHIND = 7;
    static final int INDEPENDENT_REGEX = 8;
    static final int COMMENT = 9;
    static final int CONDITIONAL_GROUP = 10;
    static final int FLAGS = 11;
    static final int CLASS_GROUP = 12;
    static final int NAMED_GROUP = 13;

    int tOffset;
    int tOutside;
    private int skip;
    private int offset;
    private int end;
    int c;

    int ttype = START;

    private char[] data;

    //results
    private int flags;
    private boolean flagsChanged;

    String groupName;
    boolean groupDeclared;

    Pretokenizer(char[] data, int offset, int end) {
        if (offset < 0 || end > data.length)
            throw new IndexOutOfBoundsException("offset=" + offset + ", end=" + end + ", length=" + data.length);
        this.offset = offset;
        this.end = end;

        this.tOffset = offset;
        this.tOutside = offset;

        this.data = data;
    }

    int flags(int def) {
        return flagsChanged ? flags : def;
    }

    void next() throws PatternSyntaxException {
        int tOffset = this.tOutside;
        int skip = this.skip;

        tOffset += skip;
        flagsChanged = false;

        int end = this.end;
        char[] data = this.data;
        boolean esc = false;
        for (int i = tOffset; i < end; i++) {
            char c = data[i];
            if (esc) {
                if(c == 'Q')
                {

                    for (; i < end; i++) {
                        char c1 = data[i];
                        if(c1 == '\\') {
                            if (i + 1 < end && data[i + 1] == 'E') {
                                i++;
                                esc = false;
                                break;
                            }
                        }
                    }
                }
                else {
                    esc = false;
                }
                continue;
            }
            switch (c) {
                case '\\':
                    esc = true;
                    continue;
                case '|':
                case ')':
                    ttype = c;
                    this.tOffset = tOffset;
                    this.tOutside = i;
                    this.skip = 1;
                    return;
                case '(':
                    if (((i + 2) < end) && (data[i + 1] == '?')) {
                        char c1 = data[i + 2];
                        switch (c1) {
                            case ':':
                                ttype = PLAIN_GROUP;
                                skip = 3; // "(?:" - skip 3 chars
                                break;
                            case '=':
                                ttype = POS_LOOKAHEAD;
                                skip = 3;  // "(?="
                                break;
                            case '!':
                                ttype = NEG_LOOKAHEAD;
                                skip = 3;  // "(?!"
                                break;
                            case '<':
                                switch (c1 = data[i + 3]) {
                                    case '=':
                                        ttype = POS_LOOKBEHIND;
                                        skip = 4; // "(?<="
                                        break;
                                    case '!':
                                        ttype = NEG_LOOKBEHIND;
                                        skip = 4; // "(?<!"
                                        break;
                                    default:
                                        throw new PatternSyntaxException("invalid character after '(?<' : " + c1);
                                }
                                break;
                            case '>':
                                ttype = INDEPENDENT_REGEX;
                                skip = 3;  // "(?>"
                                break;
                            case '#':
                                ttype = COMMENT;
                                skip = 3; // ="(?#".length, the makeTree() skips the rest by itself
                                break;
                            case '(':
                                ttype = CONDITIONAL_GROUP;
                                skip = 2; //"(?"+"(..." - skip "(?" (2 chars) and parse condition as a group
                                break;
                            case '[':
                                ttype = CLASS_GROUP;
                                skip = 2; // "(?"+"[..]+...-...&...)" - skip 2 chars and parse a class group
                                break;
                            default:
                                int mOff, mLen;
                                mLoop:
                                for (int p = i + 2; p < end; p++) {
                                    char c2 = data[p];
                                    switch (c2) {
                                        case '-':
                                        case 'i':
                                        case 'm':
                                        case 's':
                                        case 'x':
                                        case 'u':
                                        case 'X':
                                            continue mLoop;

                                        case ':':
                                            mOff = i + 2;
                                            mLen = p - mOff;
                                            if (mLen > 0) {
                                                flags = Pattern.parseFlags(data, mOff, mLen);
                                                flagsChanged = true;
                                            }
                                            ttype = PLAIN_GROUP;
                                            skip = mLen + 3; // "(?imsx:" mLen=4; skip= "(?".len + ":".len + mLen = 2+1+4=7
                                            break mLoop;
                                        case ')':
                                            flags = Pattern.parseFlags(data, mOff = (i + 2), mLen = (p - mOff));
                                            flagsChanged = true;
                                            ttype = FLAGS;
                                            skip = mLen + 3; // "(?imsx)" mLen=4, skip="(?".len+")".len+mLen=2+1+4=7
                                            break mLoop;
                                        default:
                                            throw new PatternSyntaxException("wrong char after \"(?\": " + c2);
                                    }
                                }
                                break;
                        }
                    } else if (((i + 2) < end) && (data[i + 1] == '{')) { //parse named group: ({name}....),({=name}....)
                        int p = i + 2;
                        skip = 3; //'({' + '}'
                        int nstart, nend;
                        boolean isDecl;
                        c = data[p];
                        while (Category.Z.contains(c)) {
                            c = data[++p];
                            skip++;
                            if (p == end) throw new PatternSyntaxException("malformed named group");
                        }

                        if (c == '=') {
                            isDecl = false;
                            c = data[++p];
                            skip++;
                            if (p == end) throw new PatternSyntaxException("malformed named group");
                        } else isDecl = true;

                        nstart = p;
                        while (Category.IdentifierPart.contains(c)) {
                            c = data[++p];
                            skip++;
                            if (p == end) throw new PatternSyntaxException("malformed named group");
                        }
                        nend = p;
                        while (Category.Z.contains(c)) {
                            c = data[++p];
                            skip++;
                            if (p == end) throw new PatternSyntaxException("malformed named group");
                        }
                        if (c != '}')
                            throw new PatternSyntaxException("'}' expected at " + (p - i) + " in " + new String(data, i, end - i));

                        this.groupName = new String(data, nstart, nend - nstart);
                        this.groupDeclared = isDecl;
                        ttype = NAMED_GROUP;
                    } else {
                        ttype = '(';
                        skip = 1;
                    }
                    this.tOffset = tOffset;
                    this.tOutside = i;
                    this.skip = skip;
                    return;
                case '[':
                    loop:
                    for (; ; i++) {
                        if (i == end) throw new PatternSyntaxException("malformed character class");
                        char c1 = data[i];
                        switch (c1) {
                            case '\\':
                                i++;
                                continue;
                            case ']':
                                break loop;
                        }
                    }
            }
        }
        ttype = END;
        this.tOffset = tOffset;
        this.tOutside = end;
    }

}

class Branch extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    Branch() {
        type = BRANCH;
    }

    Branch(int type) {
        switch (type) {
            case BRANCH:
            case BRANCH_STORE_CNT:
            case BRANCH_STORE_CNT_AUX1:
                this.type = type;
                break;
            default:
                throw new IllegalArgumentException("not a branch type: " + type);
        }
    }
}

class BackReference extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;
    BackReference(int no, boolean icase, boolean reverse, boolean bracket) {
        super(icase ? REG_I : REG);
        mode_reverse = reverse;
        mode_bracket = bracket;
        mode_insensitive = icase;
        memreg = no;
    }
}

class Group extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    Group() {
        this(0);
    }

    Group(int memreg) {
        type = GROUP_IN;
        this.memreg = memreg;

        //used in append()
        current = null;
        in = this;
        prev = null;

        out = new Term();
        out.type = GROUP_OUT;
        out.memreg = memreg;
    }
}

class ConditionalExpr extends Group implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    private Term node;
    private boolean newBranchStarted = false;
    private boolean linkAsBranch = true;

    ConditionalExpr(Lookahead la) {
        super(0);
      /*
      * This all is rather tricky.
      * See how this types are handled in Matcher.
      * The shortcoming is that we strongly rely upon
      * the internal structure of Lookahead.
      */
        la.in.type = LOOKAHEAD_CONDITION_IN;
        la.out.type = LOOKAHEAD_CONDITION_OUT;
        if (la.isPositive) {
            node = la.in;
            linkAsBranch = true;

            //empty 2'nd branch
            node.failNext = out;
        } else {
            node = la.out;
            linkAsBranch = false;

            //empty 2'nd branch
            node.next = out;
        }

        //node.prev=in;
        //in.next=node;

        la.prev = in;
        in.next = la;

        current = la;
        //current=node;
    }

    ConditionalExpr(Lookbehind lb) {
        super(0);
      /*
      * This all is rather tricky.
      * See how this types are handled in Matcher.
      * The shortcoming is that we strongly rely upon
      * the internal structure of Lookahead.
      */
        lb.in.type = LOOKBEHIND_CONDITION_IN;
        lb.out.type = LOOKBEHIND_CONDITION_OUT;
        if (lb.isPositive) {
            node = lb.in;
            linkAsBranch = true;

            //empty 2'nd branch
            node.failNext = out;
        } else {
            node = lb.out;
            linkAsBranch = false;

            //empty 2'nd branch
            node.next = out;
        }

        lb.prev = in;
        in.next = lb;

        current = lb;
        //current=node;
    }

    ConditionalExpr(int memreg) {
        super(0);
        Term condition = new Term(MEMREG_CONDITION);
        condition.memreg = memreg;
        condition.out = condition;
        condition.out1 = null;
        condition.branchOut = null;

        //default branch
        condition.failNext = out;

        node = current = condition;
        linkAsBranch = true;

        condition.prev = in;
        in.next = condition;

        current = condition;
    }

    protected void startNewBranch() throws PatternSyntaxException {
        if (newBranchStarted) throw new PatternSyntaxException("attempt to set a 3'd choice in a conditional expr.");
        Term node = this.node;
        node.out1 = null;
        if (linkAsBranch) {
            node.out = null;
            node.branchOut = node;
        } else {
            node.out = node;
            node.branchOut = null;
        }
        newBranchStarted = true;
        current = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ConditionalExpr that = (ConditionalExpr) o;

        return newBranchStarted == that.newBranchStarted && linkAsBranch == that.linkAsBranch && (node != null ? node.equals(that.node) : that.node == null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (node != null ? node.hashCode() : 0);
        result = 31 * result + (newBranchStarted ? 1 : 0);
        result = 31 * result + (linkAsBranch ? 1 : 0);
        return result;
    }
}

class IndependentGroup extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    IndependentGroup(int id) {
        super(0);
        in = this;
        out = new Term();
        type = INDEPENDENT_IN;
        out.type = INDEPENDENT_OUT;
        lookaheadId = out.lookaheadId = id;
    }
}

class Lookahead extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    final boolean isPositive;

    Lookahead(int id, boolean isPositive) {
        this.isPositive = isPositive;
        in = this;
        out = new Term();
        if (isPositive) {
            type = PLOOKAHEAD_IN;
            out.type = PLOOKAHEAD_OUT;
        } else {
            type = NLOOKAHEAD_IN;
            out.type = NLOOKAHEAD_OUT;
            branchOut = this;
        }
        lookaheadId = id;
        out.lookaheadId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Lookahead lookahead = (Lookahead) o;

        return isPositive == lookahead.isPositive;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isPositive ? 1 : 0);
        return result;
    }
}

class Lookbehind extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    final boolean isPositive;
    private int prevDistance = -1;

    Lookbehind(int id, boolean isPositive) {
        distance = 0;
        this.isPositive = isPositive;
        in = this;
        out = new Term();
        if (isPositive) {
            type = PLOOKBEHIND_IN;
            out.type = PLOOKBEHIND_OUT;
        } else {
            type = NLOOKBEHIND_IN;
            out.type = NLOOKBEHIND_OUT;
            branchOut = this;
        }
        lookaheadId = id;
        out.lookaheadId = id;
    }

    protected Term append(Term t) throws PatternSyntaxException {
        distance += length(t);
        return super.append(t);
    }

    protected Term replaceCurrent(Term t) throws PatternSyntaxException {
        distance += length(t) - length(current);
        return super.replaceCurrent(t);
    }

    private static int length(Term t) throws PatternSyntaxException {
        int type = t.type;
        switch (type) {
            case CHAR:
            case BITSET:
            case BITSET2:
            case ANY_CHAR:
            case ANY_CHAR_NE:
                return 1;
            case BOUNDARY:
            case DIRECTION:
            case UBOUNDARY:
            case UDIRECTION:
                return 0;
            default:
                if (type >= FIRST_TRANSPARENT && type <= LAST_TRANSPARENT) return 0;
                throw new PatternSyntaxException("variable length element within a lookbehind assertion");
        }
    }

    protected void startNewBranch() throws PatternSyntaxException {
        prevDistance = distance;
        distance = 0;
        super.startNewBranch();
    }

    protected void close() throws PatternSyntaxException {
        int pd = prevDistance;
        if (pd >= 0) {
            if (distance != pd)
                throw new PatternSyntaxException("non-equal branch lengths within a lookbehind assertion");
        }
        super.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Lookbehind that = (Lookbehind) o;

        return isPositive == that.isPositive && prevDistance == that.prevDistance;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isPositive ? 1 : 0);
        result = 31 * result + prevDistance;
        return result;
    }
}

class TermIterator extends Term implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    TermIterator(Term term, int min, int max, ArrayList<TermIterator> collection) throws PatternSyntaxException {
        collection.add(this);
        switch (term.type) {
            case CHAR:
            case ANY_CHAR:
            case ANY_CHAR_NE:
            case BITSET:
            case BITSET2: {
                target = term;
                Term back = new Term();
                if (min <= 0 && max < 0) {
                    type = REPEAT_0_INF;
                    back.type = BACKTRACK_0;
                } else if (min > 0 && max < 0) {
                    type = REPEAT_MIN_INF;
                    back.type = BACKTRACK_MIN;
                    minCount = back.minCount = min;
                } else {
                    type = REPEAT_MIN_MAX;
                    back.type = BACKTRACK_MIN;
                    minCount = back.minCount = min;
                    maxCount = max;
                }

                failNext = back;

                in = this;
                out = this;
                out1 = back;
                branchOut = null;
                return;
            }
            case REG: {
                target = term;
                memreg = term.memreg;
                Term back = new Term();
                if (max < 0) {
                    type = REPEAT_REG_MIN_INF;
                    back.type = BACKTRACK_REG_MIN;
                    minCount = back.minCount = min;
                } else {
                    type = REPEAT_REG_MIN_MAX;
                    back.type = BACKTRACK_REG_MIN;
                    minCount = back.minCount = min;
                    maxCount = max;
                }

                failNext = back;

                in = this;
                out = this;
                out1 = back;
                branchOut = null;
                return;
            }
            default:
                throw new PatternSyntaxException("can't iterate this type: " + term.type);
        }
    }

    void optimize() {
//BACKTRACK_MIN_REG_FIND
        Term back = failNext;
        Optimizer opt = Optimizer.find(back.next);
        if (opt == null) return;
        failNext = opt.makeBacktrack(back);
    }

}