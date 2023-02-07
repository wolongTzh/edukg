package com.tsinghua.edukg.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HanlpHelper {

    //抽取问题中的关键词 策略:一级重要(名词\术语等),与查询;二级重要(形容词等),或查询;不重要(语气词,助词,代词等),不查询
    //一级重要
    static List<Nature> firstClassNatureList = new ArrayList<>(Arrays.asList(
            Nature.a,    /*	 形容词	*/
            Nature.f,    /*	 方位词	*/
            Nature.g,    /*	 学术词汇	*/
            Nature.gb,    /*	 生物相关词汇	*/
            Nature.gbc,    /*	 生物类别	*/
            Nature.gc,    /*	 化学相关词汇	*/
            Nature.gg,    /*	 地理地质相关词汇	*/
            Nature.gi,    /*	 计算机相关词汇	*/
            Nature.gm,    /*	 数学相关词汇	*/
            Nature.gp,    /*	 物理相关词汇	*/
            Nature.i,    /*	 成语	*/
            Nature.j,    /*	 简称略语	*/
            Nature.n,    /*	 名词	*/
            Nature.nb,    /*	 生物名	*/
            Nature.nba,    /*	 动物名	*/
            Nature.nbc,    /*	 动物纲目	*/
            Nature.nbp,    /*	 植物名	*/
            Nature.nf,    /*	 食品，比如“薯片”	*/
            Nature.ni,    /*	 机构相关（不是独立机构名）	*/
            Nature.nic,    /*	 下属机构	*/
            Nature.nis,    /*	 机构后缀	*/
            Nature.nit,    /*	 教育相关机构	*/
            Nature.nl,    /*	 名词性惯用语	*/
            Nature.nm,    /*	 物品名	*/
            Nature.nmc,    /*	 化学品名	*/
            Nature.nn,    /*	 工作相关名词	*/
            Nature.nnd,    /*	 职业	*/
            Nature.nnt,    /*	 职务职称	*/
            Nature.nr,    /*	 人名	*/
            Nature.nr1,    /*	 复姓	*/
            Nature.nr2,    /*	 蒙古姓名	*/
            Nature.nrf,    /*	 音译人名	*/
            Nature.nrj,    /*	 日语人名	*/
            Nature.ns,    /*	 地名	*/
            Nature.nsf,    /*	 音译地名	*/
            Nature.nt,    /*	 机构团体名	*/
            Nature.ntc,    /*	 公司名	*/
            Nature.ntcb,    /*	 银行	*/
            Nature.ntcf,    /*	 工厂	*/
            Nature.ntch,    /*	 酒店宾馆	*/
            Nature.nth,    /*	 医院	*/
            Nature.nto,    /*	 政府机构	*/
            Nature.nts,    /*	 中小学	*/
            Nature.ntu,    /*	 大学	*/
            Nature.t,    /*	 时间词	*/ //唐朝 三国
            Nature.vn    /*	 名动词	*/ //毗邻

    ));
    //二级重要
    static List<Nature> secondClassNatureList = new ArrayList<>(Arrays.asList(
            Nature.ad,    /*	 副形词	*/
            Nature.m,    /*	 数词	*/
            Nature.Mg,    /*	 甲乙丙丁之类的数词	*/
            Nature.mq,    /*	 数量词	*/
            Nature.l,    /*	 习用语	*/
            Nature.mg,    /*	 数语素	*/
            Nature.ng,    /*	 名词性语素	*/
            Nature.nh,    /*	 医药疾病等健康相关名词	*/
            Nature.nhd,    /*	 疾病	*/
            Nature.nhm,    /*	 药品	*/
            Nature.nz,    /*	 其他专名	*/ //多少年
            Nature.s,    /*	 处所词	*/
            Nature.v,    /*	 动词	*/
            Nature.vd,    /*	 副动词	*/
            Nature.vf,    /*	 趋向动词	*/
            Nature.vg,    /*	 动词性语素	*/
            Nature.vi,    /*	 不及物动词（内动词）	*/
            Nature.vl    /*	 动词性惯用语	*/
    ));
    //三级重要
    static List<Nature> thirdClassNatureList = new ArrayList<>(Arrays.asList(

            Nature.ag,    /*	 形容词性语素	*/
            Nature.al,    /*	 形容词性惯用语	*/
            Nature.an,    /*	 名形词	*/
            Nature.b,    /*	 区别词	*/
            Nature.begin,    /*	 仅用于始##始，不会出现在分词结果中	*/
            Nature.bg,    /*	 区别语素	*/
            Nature.bl,    /*	 区别词性惯用语	*/
            Nature.c,    /*	 连词	*/
            Nature.cc,    /*	 并列连词	*/
            Nature.d,    /*	 副词	*/
            Nature.dg,    /*	 辄,俱,复之类的副词	*/
            Nature.dl,    /*	 连语	*/
            Nature.e,    /*	 叹词	*/
            Nature.end,    /*	 仅用于终##终，不会出现在分词结果中	*/
            Nature.h,    /*	 前缀	*/
            Nature.k,    /*	 后缀	*/
            Nature.nx,    /*	 字母专名	*/
            Nature.o,    /*	 拟声词	*/
            Nature.p,    /*	 介词	*/
            Nature.pba,    /*	 介词“把”	*/
            Nature.pbei,    /*	 介词“被”	*/
            Nature.q,    /*	 量词	*/  //摄氏度的符号
            Nature.qg,    /*	 量词语素	*/
            Nature.qt,    /*	 时量词	*/
            Nature.qv,    /*	 动量词	*/
            Nature.tg,    /*	 时间词性语素	*/
            Nature.u,    /*	 助词	*/
            Nature.ud,    /*	 助词	*/
            Nature.ude1,    /*	 的 底	*/
            Nature.ude2,    /*	 地	*/
            Nature.ude3,    /*	 得	*/
            Nature.udeng,    /*	 等 等等 云云	*/
            Nature.udh,    /*	 的话	*/
            Nature.ug,    /*	 过	*/
            Nature.uguo,    /*	 过	*/
            Nature.uj,    /*	 助词	*/
            Nature.ul,    /*	 连词	*/
            Nature.ule,    /*	 了 喽	*/
            Nature.ulian,    /*	 连 （“连小学生都会”）	*/
            Nature.uls,    /*	 来讲 来说 而言 说来	*/
            Nature.usuo,    /*	 所	*/
            Nature.uv,    /*	 连词	*/
            Nature.uyy,    /*	 一样 一般 似的 般	*/
            Nature.uz,    /*	 着	*/
            Nature.uzhe,    /*	 着	*/
            Nature.uzhi,    /*	 之	*/
            Nature.vshi,    /*	 动词“是”	*/
            Nature.vx,    /*	 形式动词	*/
            Nature.vyou,    /*	 动词“有”	*/
            Nature.w,    /*	 标点符号	*/
            Nature.wb,    /*	 百分号千分号，全角：％ ‰   半角：%	*/
            Nature.wd,    /*	 逗号，全角：， 半角：,	*/
            Nature.wf,    /*	 分号，全角：； 半角： ;	*/
            Nature.wh,    /*	 单位符号，全角：￥ ＄ ￡  °  ℃  半角：$	*/
            Nature.wj,    /*	 句号，全角：。	*/
            Nature.wky,    /*	 右括号，全角：） 〕  ］ ｝ 》  】 〗 〉 半角： ) ] { >	*/
            Nature.wkz,    /*	 左括号，全角：（ 〔  ［  ｛  《 【  〖 〈   半角：( [ { <	*/
            Nature.wm,    /*	 冒号，全角：： 半角： :	*/
            Nature.wn,    /*	 顿号，全角：、	*/
            Nature.wp,    /*	 破折号，全角：——   －－   ——－   半角：---  ----	*/
            Nature.ws,    /*	 省略号，全角：……  …	*/
            Nature.wt,    /*	 叹号，全角：！ 半角：!	*/
            Nature.ww,    /*	 问号，全角：？ 半角：?	*/
            Nature.wyy,    /*	 右引号，全角：” ’ 』	*/
            Nature.wyz,    /*	 左引号，全角：“ ‘ 『	*/
            Nature.x,    /*	 字符串	*/
            Nature.xu,    /*	 网址URL	*/
            Nature.xx,    /*	 非语素字	*/
            Nature.y,    /*	 语气词(delete yg)	*/
            Nature.yg,    /*	 语气语素	*/
            Nature.z,    /*	 状态词	*/
            Nature.zg    /*	 状态词	*/
    ));

    //代词 用处是将代词之后的名词给去掉
    static List<Nature> rNatureList = new ArrayList<>(Arrays.asList(
            Nature.r,    /*	 代词	*/
            Nature.rg,    /*	 代词性语素	*/
            Nature.Rg,    /*	 古汉语代词性语素	*/
            Nature.rr,    /*	 人称代词	*/
            Nature.ry,    /*	 疑问代词	*/
            Nature.rys,    /*	 处所疑问代词	*/
            Nature.ryt,    /*	 时间疑问代词	*/
            Nature.ryv,    /*	 谓词性疑问代词	*/
            Nature.rz,    /*	 指示代词	*/
            Nature.rzs,    /*	 处所指示代词	*/
            Nature.rzt,    /*	 时间指示代词	*/
            Nature.rzv    /*	 谓词性指示代词	*/
    ));

    //停用词
    public static List<String> stopWords = new ArrayList<>(Arrays.asList(
            "是"
    ));

    public static String CutWordRetNeedConcernWords(String text) {
        List<Term> words = HanLP.segment(text);
        String needConcernWords = "";
        for(Term word : words) {
            if(stopWords.contains(word.word)) {
                continue;
            }
            if(firstClassNatureList.contains(word.nature)) {
                needConcernWords += word.word + " ";
            }
            else if(secondClassNatureList.contains(word.nature)) {
                needConcernWords += word.word + " ";
            }
        }
        return needConcernWords.trim();
    }

    public static List<Term> cutWords(String source) {
        return HanLP.segment(source);
    }
}
