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

import java.util.Arrays;

class BlockSet implements UnicodeConstants {
    /*
    private static final Block[][] categoryBits = new Block[CATEGORY_COUNT][BLOCK_COUNT];

    static {
        for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
            int cat = Character.getType((char) i);
            int blockNo = (i >> 8) & 0xff;
            Block b = categoryBits[cat][blockNo];
            if (b == null) categoryBits[cat][blockNo] = b = new Block();
            b.set(i & 0xff);
        }
    }*/

    private boolean positive = true;
    private boolean isLarge = false;

    private IntBitSet block0 = new IntBitSet();  //1-byte bit set
    private static final IntBitSet emptyBlock0 = new IntBitSet();

    private Block[] blocks;  //2-byte bit set



    private int weight;

    final void reset() {
        positive = true;
        block0 = null;
        blocks = null;
        isLarge = false;
        weight = 0;
    }

    static void unify(BlockSet bs, Term term) {
        if (bs.isLarge) {
            term.type = Term.BITSET2;
            term.bitset2 = Block.toBitset2(bs.blocks);
        } else {
            term.type = Term.BITSET;
            term.bitset = bs.block0 == null ? emptyBlock0 : bs.block0;
        }
        term.inverse = !bs.positive;
        term.weight = bs.positive ? bs.weight : MAX_WEIGHT - bs.weight;
    }

    final void setPositive(boolean b) {
        positive = b;
    }

    final boolean isPositive() {
        return positive;
    }

    final boolean isLarge() {
        return isLarge;
    }

    private void enableLargeMode() {
        if (isLarge) return;
        Block[] blocks = new Block[BLOCK_COUNT];
        this.blocks = blocks;
        if (block0 != null) {
            blocks[0] = new Block(block0);
        }
        isLarge = true;
    }

    private int getWeight() {
        return positive ? weight : MAX_WEIGHT - weight;
    }

    final void setWordChar(boolean unicode) {
        if (unicode) {
            if (!isLarge) enableLargeMode();
            weight += Block.add(this.blocks, Category.Word.blocks, 0, BLOCK_COUNT - 1, false);
            /*
            setCategory("Lu");
            setCategory("Ll");
            setCategory("Lt");
            setCategory("Lo");
            setCategory("Nd");
            setChar('_');*/
        } else {
            setRange('a', 'z');
            setRange('A', 'Z');
            setRange('0', '9');
            setChar('_');
        }
    }

    final void setDigit(boolean unicode) {
        if (unicode) {
            setCategory("Nd");
        } else {
            setRange('0', '9');
        }
    }

    final void setSpace(boolean unicode) {
        if (unicode) {
            setCategory("Zs");
            setCategory("Zp");
            setCategory("Zl");
        } else {
            setChar(' ');
            setChar('\r');
            setChar('\n');
            setChar('\t');
            setChar('\f');
        }
    }

    final void setHorizontalSpace(boolean unicode) {
        if (unicode) {
            setCategory("Zh");
        } else {
            setChar(' ');
            setChar('\t');
        }
    }
    final void setVerticalSpace(boolean unicode) {
        if (unicode) {
            setCategory("Zv");
        } else {
            setChar('\n');
            setChar('\r');
            setChar('\f');
            setChar('\u000B');
        }
    }

    final void setCategory(String c) {
        if (!isLarge) enableLargeMode();
        Block[] catBits = Category.categories.get(c).blocks;
        weight += Block.add(this.blocks, catBits, 0, BLOCK_COUNT - 1, false);
//System.out.println("["+this+"].setCategory("+c+"): weight="+weight);
    }

    final void setChars(String chars) {
        for (int i = chars.length() - 1; i >= 0; i--) setChar(chars.charAt(i));
    }

    final void setChar(char c) {
        setRange(c, c);
    }

    final void setRange(char c1, char c2) {
//System.out.println("["+this+"].setRange("+c1+","+c2+"):");
//if(c1>31 && c1<=126 && c2>31 && c2<=126) System.out.println("setRange('"+c1+"','"+c2+"'):");
//else System.out.println("setRange(["+Integer.toHexString(c1)+"],["+Integer.toHexString(c2)+"]):");
        if (c2 >= 256 || isLarge) {
            int s = 0;
            if (!isLarge) {
                enableLargeMode();
            }
            Block[] blocks = this.blocks;
            for (int c = c1; c <= c2; c++) {
                int i2 = (c >> 8) & 0xff;
                int i = c & 0xff;
                Block block = blocks[i2];
                if (block == null) {
                    blocks[i2] = block = new Block();
                }
                if (block.set(i)) s++;
            }
            weight += s;
        } else {
            IntBitSet block0 = this.block0;
            if (block0 == null) {
                this.block0 = block0 = new IntBitSet(BLOCK_SIZE);
            }
            weight += set(block0, c1, c2);
        }
    }

    final void add(BlockSet bs) {
        add(bs, false);
    }

    final void add(BlockSet bs, boolean inverse) {
        weight += addImpl(this, bs, !bs.positive ^ inverse);
    }

    private static int addImpl(BlockSet bs1, BlockSet bs2, boolean inv) {
        int s = 0;
        if (!bs1.isLarge && !bs2.isLarge && !inv) {
            if (bs2.block0 != null) {
                IntBitSet bits = bs1.block0;
                if (bits == null) bs1.block0 = bits = new IntBitSet(BLOCK_SIZE);
                s += add(bits, bs2.block0, 0, BLOCK_SIZE - 1, false);
            }
        } else {
            if (!bs1.isLarge) bs1.enableLargeMode();
            if (!bs2.isLarge) bs2.enableLargeMode();
            s += Block.add(bs1.blocks, bs2.blocks, 0, BLOCK_COUNT - 1, inv);
        }
        return s;
    }

    final void subtract(BlockSet bs) {
        subtract(bs, false);
    }

    private void subtract(BlockSet bs, boolean inverse) {
//System.out.println("["+this+"].subtract(["+bs+"],"+inverse+"):");
        weight += subtractImpl(this, bs, !bs.positive ^ inverse);
    }

    private static int subtractImpl(BlockSet bs1, BlockSet bs2, boolean inv) {
        int s = 0;
        if (!bs1.isLarge && !bs2.isLarge && !inv) {
            IntBitSet bits1, bits2;
            if ((bits2 = bs2.block0) != null) {
                bits1 = bs1.block0;
                if (bits1 == null) return 0;
                s += subtract(bits1, bits2, false);
            }
        } else {
            if (!bs1.isLarge) bs1.enableLargeMode();
            if (!bs2.isLarge) bs2.enableLargeMode();
            s += Block.subtract(bs1.blocks, bs2.blocks, 0, BLOCK_COUNT - 1, inv);
        }
        return s;
    }

    final void intersect(BlockSet bs) {
        intersect(bs, false);
    }

    private void intersect(BlockSet bs, boolean inverse) {
//System.out.println("["+this+"].intersect(["+bs+"],"+inverse+"):");
        subtract(bs, !inverse);
    }

    static int add(IntBitSet bs1, IntBitSet bs2, int from, int to, boolean inv) {
        int s = bs1.cardinality();
        if (inv)
            bs1.or(bs2.clone().negate());
        else
            bs1.or(bs2);
        return bs1.cardinality() - s;
    }

    static int subtract(IntBitSet bs1, IntBitSet bs2, boolean inv) {
        int s = -bs1.cardinality();
        if(inv)
            bs1.andNot(bs2.clone().negate());
        else
            bs1.andNot(bs2);
        return s + bs1.cardinality();
    }

    private static int set(IntBitSet arr, int from, int to) {
        int s = arr.cardinality();
        arr.set(from, to);
        return arr.cardinality() - s;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!positive) sb.append('^');

        if (isLarge) sb.append(CharacterClass.stringValue2(Block.toBitset2(blocks)));
        else if (block0 != null) sb.append(CharacterClass.stringValue0(block0));

        sb.append('(');
        sb.append(getWeight());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockSet blockSet = (BlockSet) o;

        if (positive != blockSet.positive) return false;
        if (isLarge != blockSet.isLarge) return false;
        if (weight != blockSet.weight) return false;
        if (block0 != null ? !block0.equals(blockSet.block0) : blockSet.block0 != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(blocks, blockSet.blocks);

    }

    @Override
    public int hashCode() {
        int result = (positive ? 1 : 0);
        result = 31 * result + (isLarge ? 1 : 0);
        result = 31 * result + (block0 != null ? block0.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(blocks);
        result = 31 * result + weight;
        return result;
    }

    /*
   public static void main(String[] args){
      //System.out.print("blocks(Lu)=");
      //System.out.println(CharacterClass.stringValue2(Block.toBitset2(categoryBits[Lu])));
      //System.out.println("[1][0].get('a')="+categoryBits[1][0].get('a'));
      //System.out.println("[1][0].get('A')="+categoryBits[1][0].get('A'));
      //System.out.println("[1][0].get(65)="+categoryBits[1][0].get(65));
      //System.out.println(""+categoryBits[1][0].get('A'));
      BlockSet b1=new BlockSet();
      //b1.setCategory(Lu);
      //b1.enableLargeMode();
      b1.setRange('a','z');
      b1.setRange('\u00E0','\u00FF');
      
      BlockSet b2=new BlockSet();
      //b2.setCategory(Ll);
      //b2.enableLargeMode();
      b2.setRange('A','Z');
      b2.setRange('\u00C0','\u00DF');
      
      BlockSet b=new BlockSet();
      //bs.setRange('a','z');
      //bs.setRange('A','Z');
      b.add(b1);
      b.add(b2,true);
      
      System.out.println("b1="+b1);
      System.out.println("b2="+b2);
      System.out.println("b=b1+^b2="+b);
      
      b.subtract(b1,true);
      
      System.out.println("(b1+^b2)-^b1="+b);
      
   }
   */
}

class Block implements UnicodeConstants {
    private boolean isFull;
    //private boolean[] bits;
    private IntBitSet bits;
    private boolean shared = false;

    Block() {
    }

    Block(IntBitSet bits) {
        this.bits = bits;
        shared = true;
    }

    final boolean set(int c) {
//System.out.println("Block.add("+CharacterClass.stringValue2(toBitset2(targets))+","+CharacterClass.stringValue2(toBitset2(addends))+","+from*BLOCK_SIZE+","+to*BLOCK_SIZE+","+inv+"):");
        if (isFull) return false;
        IntBitSet bits = this.bits;
        if (bits == null) {
            this.bits = bits = new IntBitSet(BLOCK_SIZE);
            shared = false;
            bits.set(c);
            return true;
        }

        if (bits.get(c)) return false;

        if (shared) bits = copyBits(this);

        bits.set(c);
        return true;
    }

    final boolean get(int c) {
        return isFull || (bits != null && bits.get(c));
    }

    static int add(Block[] targets, Block[] addends, int from, int to, boolean inv) {
        int s = 0;
        for (int i = from; i <= to; i++) {
            Block addend = addends[i];
            if (addend == null) {
                if (!inv) continue;
            } else if (addend.isFull && inv) continue;

            Block target = targets[i];
            if (target == null) targets[i] = target = new Block();
            else if (target.isFull) continue;

            s += add(target, addend, inv);
        }
        return s;
    }

    private static int add(Block target, Block addend, boolean inv) {
        //there is provided that !target.isFull
        IntBitSet targetbits, addbits;
        if (addend == null) {
            if (!inv) return 0;
            int s = BLOCK_SIZE;
            if ((targetbits = target.bits) != null) {
                s -= count(targetbits, 0, BLOCK_SIZE - 1);
            }
            target.isFull = true;
            target.bits = null;
            target.shared = false;
            return s;
        } else if (addend.isFull) {
            if (inv) return 0;
            int s = BLOCK_SIZE;
            if ((targetbits = target.bits) != null) {
                s -= count(targetbits, 0, BLOCK_SIZE - 1);
            }
            target.isFull = true;
            target.bits = null;
            target.shared = false;
            return s;
        } else if ((addbits = addend.bits) == null) {
            if (!inv) return 0;
            int s = BLOCK_SIZE;
            if ((targetbits = target.bits) != null) {
                s -= count(targetbits, 0, BLOCK_SIZE - 1);
            }
            target.isFull = true;
            target.bits = null;
            target.shared = false;
            return s;
        } else {
            if ((targetbits = target.bits) == null) {
                if (!inv) {
                    target.bits = addbits;
                    target.shared = true;
                    return count(addbits, 0, BLOCK_SIZE - 1);
                } else {
                    target.bits = targetbits = emptyBits(null);
                    target.shared = false;
                    return BlockSet.add(targetbits, addbits, 0, BLOCK_SIZE - 1, inv);
                }
            } else {
                if (target.shared) targetbits = copyBits(target);
                return BlockSet.add(targetbits, addbits, 0, BLOCK_SIZE - 1, inv);
            }
        }
    }

    static int subtract(Block[] targets, Block[] subtrahends, int from, int to, boolean inv) {
        int s = 0;
        for (int i = from; i <= to; i++) {
            Block target = targets[i];
            if (target == null || (!target.isFull && target.bits == null)) continue;

            Block subtrahend = subtrahends[i];

            if (subtrahend == null) {
                if (inv) {
                    if (target.isFull) {
                        s -= BLOCK_SIZE;
                    } else {
                        s -= count(target.bits, 0, BLOCK_SIZE - 1);
                    }
                    target.isFull = false;
                    target.bits = null;
                    target.shared = false;
                }
            } else {
                s += subtract(target, subtrahend, inv);
            }
        }
        return s;
    }

    private static int subtract(Block target, Block subtrahend, boolean inv) {
        IntBitSet targetbits, subbits;
        //there is provided that target.isFull or target.bits!=null
        if (subtrahend.isFull) {
            if (inv) return 0;
            int s = 0;
            if (target.isFull) {
                s = BLOCK_SIZE;
            } else {
                s = target.bits.cardinality();
            }
            target.isFull = false;
            target.bits = null;
            target.shared = false;
            return s;
        } else if ((subbits = subtrahend.bits) == null) {
            if (!inv) return 0;
            int s = 0;
            if (target.isFull) {
                s = BLOCK_SIZE;
            } else {
                s = target.bits.cardinality();
            }
            target.isFull = false;
            target.bits = null;
            target.shared = false;
            return s;
        } else {
            if (target.isFull) {
                IntBitSet bits = fullBits(target.bits);
                int s = BlockSet.subtract(bits, subbits, inv);
                target.isFull = false;
                target.shared = false;
                target.bits = bits;
                return s;
            } else {
                if (target.shared) targetbits = copyBits(target);
                else targetbits = target.bits;
                return BlockSet.subtract(targetbits, subbits, inv);
            }
        }
    }

    private static IntBitSet copyBits(Block block) {
        IntBitSet bits = block.bits.clone();
        block.bits = bits;
        block.shared = false;
        return bits;
    }

    private static IntBitSet fullBits(IntBitSet bits) {
        if (bits == null) bits = new IntBitSet(BLOCK_SIZE);
        bits.set(0, BLOCK_SIZE);
        return bits;
    }

    private static IntBitSet emptyBits(IntBitSet bits) {
        if (bits == null) bits = new IntBitSet(BLOCK_SIZE);
        else bits.clear();
        return bits;
    }

    private static int count(IntBitSet arr, int from, int to) {
        int s = 0;
        for (int i = from; i <= to; i++) {
            if (arr.get(i)) s++;
        }
        return s;
    }

    static IntBitSet[] toBitset2(Block[] blocks) {
        int len = blocks.length;
        IntBitSet[] result = new IntBitSet[len];
        for (int i = 0; i < len; i++) {
            Block block = blocks[i];
            if (block == null) continue;
            if (block.isFull) {
                result[i] = FULL_BITS;
            } else result[i] = block.bits;
        }
        return result;
    }

    private final static IntBitSet EMPTY_BITS = new IntBitSet(BLOCK_SIZE);
    private final static IntBitSet FULL_BITS = new IntBitSet(BLOCK_SIZE);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        if (isFull != block.isFull) return false;
        if (shared != block.shared) return false;
        return bits != null ? bits.equals(block.bits) : block.bits == null;

    }

    @Override
    public int hashCode() {
        int result = (isFull ? 1 : 0);
        result = 31 * result + (bits != null ? bits.hashCode() : 0);
        result = 31 * result + (shared ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "isFull=" + isFull +
                ", bits=" + bits +
                ", shared=" + shared +
                '}';
    }

    static {
        FULL_BITS.set(0, BLOCK_SIZE-1);
    }
}
