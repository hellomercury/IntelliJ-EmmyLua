package com.tang.intellij.lua.psi.stub.elements;

import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IStubFileElementType;
import com.tang.intellij.lua.lang.LuaLanguage;
import com.tang.intellij.lua.psi.LuaFile;
import com.tang.intellij.lua.psi.stub.LuaFileStub;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Created by tangzx on 2016/11/27.
 */
public class LuaFileStubElementType extends IStubFileElementType<LuaFileStub> {
    public LuaFileStubElementType() {
        super(LuaLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new DefaultStubBuilder(){
            @NotNull
            @Override
            protected StubElement createStubForFile(@NotNull PsiFile file) {
                if (file instanceof LuaFile)
                    return new LuaFileStub((LuaFile) file);
                return super.createStubForFile(file);
            }
        };
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "lua.file";
    }
}