package com.tang.intellij.lua.debugger.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.impl.XSourcePositionImpl;
import com.tang.intellij.lua.debugger.LuaDebugProcess;
import com.tang.intellij.lua.debugger.LuaExecutionStack;
import com.tang.intellij.lua.debugger.LuaStackFrame;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Created by tangzx on 2016/12/31.
 */
public class GetStackCommand extends DebugCommand {
    private String[] pauseInfo;

    public GetStackCommand(String[] params) {
        pauseInfo = params;
    }

    @Override
    public void write(OutputStreamWriter writer) throws IOException {
        writer.write("STACK");
    }

    @Override
    public void handle(String data) {
        Pattern pattern = Pattern.compile("(\\d+) (\\w+) (.+)");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            //String status = matcher.group(1);//200
            //String statusName = matcher.group(2);//OK
            String stackCode = matcher.group(3);
            stackCode = stackCode.replace("--[[..skipped..]]", "");

            String finalStackCode = stackCode;
            ApplicationManager.getApplication().runReadAction(() -> {
                LuaFile file = LuaElementFactory.createFile(debugProcess.getSession().getProject(), finalStackCode);
                PsiElement firstChild = file.getFirstChild();

                StackVisitor stackVisitor = new StackVisitor(debugProcess);
                firstChild.accept(stackVisitor);

                //top frame
                List<LuaStackFrame> list = new ArrayList<>();
                Project project = debugProcess.getSession().getProject();
                PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, pauseInfo[1], new ProjectAndLibrariesScope(project));
                if (psiFiles.length > 0) {
                    int line = Integer.parseInt(pauseInfo[2]);
                    PsiFile pauseFile = psiFiles[0];
                    XSourcePosition position = XSourcePositionImpl.create(pauseFile.getVirtualFile(), line - 1);
                    list.add(new LuaStackFrame(position));
                }
                list.addAll(stackVisitor.stackFrameList);

                debugProcess.setStack(new LuaExecutionStack(list));
            });
        }
    }

    class StackVisitor extends LuaVisitor {
        int tableLevel = 0;
        boolean isStackData;
        int stackChildIndex = 0;
        int stackInfoIndex = 0;

        LuaStackFrame stackFrame;
        String functionName;
        String fileName;
        int line;
        int col;

        private List<LuaStackFrame> stackFrameList = new ArrayList<>();
        private LuaDebugProcess process;

        StackVisitor(LuaDebugProcess process) {
            this.process = process;
        }


        @Override
        public void visitElement(PsiElement element) {
            element.acceptChildren(this);
        }

        @Override
        public void visitTableConstructor(@NotNull LuaTableConstructor o) {
            tableLevel++;
            if (tableLevel == 2) {
                isStackData = true;
                stackChildIndex = -1;
            } else if (tableLevel == 3) {
                stackChildIndex++;
            }

            LuaFieldList fieldList = o.getFieldList();
            if (fieldList != null) {
                visitFieldList(fieldList);
            }

            if (tableLevel == 2) {
                isStackData = false;
            }

            tableLevel--;
        }

        @Override
        public void visitFieldList(@NotNull LuaFieldList o) {
            if (isStackData) {
                if (stackChildIndex == 0) {
                    System.out.println(o.getText());
                    stackInfoIndex = 0;
                    super.visitFieldList(o);

                    Project project = process.getSession().getProject();
                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, new ProjectAndLibrariesScope(project));
                    if (psiFiles.length > 0) {
                        PsiFile file = psiFiles[0];
                        XSourcePosition position = XSourcePositionImpl.create(file.getVirtualFile(), line - 1);
                        stackFrame = new LuaStackFrame(position);
                        stackFrameList.add(stackFrame);
                    }
                    return;
                }
            }

            super.visitFieldList(o);
        }

        @Override
        public void visitTableField(@NotNull LuaTableField o) {
            if (isStackData) {
                if (stackChildIndex == 0) {
                    String text = o.getText();
                    switch (stackInfoIndex) {
                        case 0: functionName = text.substring(1, text.length() - 1); break;
                        case 1: fileName = text.substring(1, text.length() - 1); break;
                        case 3: line = Integer.parseInt(text); break;
                    }
                    stackInfoIndex++;
                    return;
                } else if (stackChildIndex == 2) {
                    //todo
                }
            }
            super.visitTableField(o);
        }
    }
}