package com.nowcoderstudy.webcommunity.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode trie = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode node = trie;
        int length = keyword.length();
        for(int i=0;i<length;i++){
            char c = keyword.charAt(i);
            TrieNode subNode = node.getSubNode(c);
            if(subNode==null){
                subNode = new TrieNode();
                node.addSubNode(c,subNode);
            }
            node = subNode;
            if(i==length-1){
                subNode.setKeywordEnd(true);
            }
        }
    }

    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public String replaceSensitive(String in){
        if(StringUtils.isBlank(in)){
            return null;
        }
        StringBuilder out = new StringBuilder();
        int length = in.length();
        TrieNode node = trie;
        int begin=0;
        int position=0;
        while(begin <length){
            if(position==length){
                out.append(in.charAt(begin));
                node=trie;
                begin++;
                position=begin;
                continue;
            }
            char c = in.charAt(position);
            if(isSymbol(c)){
                if(node==trie){
                    out.append(c);
                    begin++;
                    //position=begin;
                }
//                else{
//                    out.append(in.charAt(begin));
//                    begin++;
//                    position=begin;
//                    node=trie;
//                }
                position++;
                continue;
            }
            node = node.getSubNode(c);
            if(node==null){
                out.append(in.charAt(begin));
                begin++;
                position=begin;
                node=trie;
            }
            else if(node.isKeywordEnd()){
                out.append(REPLACEMENT);
                node=trie;
                begin=position+1;
                position++;
            }
            else{
                position++;
            }
        }
        return out.toString();


    }


    // 前缀树
    private class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }
}
