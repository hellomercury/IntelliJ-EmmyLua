package com.tang.intellij.lua.reference;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.tang.intellij.lua.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * Created by TangZX on 2016/11/22.
 */
public class LuaIdentifierRef extends ASTWrapperPsiElement implements PsiReference, com.tang.intellij.lua.psi.LuaIdentifierRef {

    public LuaIdentifierRef(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    @Override
    public PsiElement getElement() {
        return this;
    }

    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, getTextLength());
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        LuaLocalDef localDef = PsiTreeUtil.findChildOfType(getContainingFile(), LuaLocalDef.class);

        return PsiTreeUtil.findChildOfType(localDef, LuaNameDef.class);
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return null;
    }

    @Override
    public PsiElement handleElementRename(String s) throws IncorrectOperationException {
        PsiElement newId = LuaElementFactory.createIdentifier(getProject(), s);
        getId().replace(newId);
        return newId;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isReferenceTo(PsiElement psiElement) {
        return true;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @NotNull
    @Override
    public PsiElement getId() {
        return findChildByType(LuaTypes.ID);
    }
}
