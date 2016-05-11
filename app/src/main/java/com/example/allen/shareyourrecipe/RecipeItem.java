package com.example.allen.shareyourrecipe;

public class RecipeItem {

    /* Variable Declarations*/

    private String mId_;
    private String mRecipeName_;
    private String mRecipeImage_;
    private String mAuthor_;
    private String mDateCreated_;
    private String mIngredients_;
    private String mDescription_;
    private String mUserId_;

    /* Member Method Definitions */

    public String getId() {return this.mId_;}

    public String getRecipeName() {return this.mRecipeName_;}

    public String getRecipeImage() {return this.mRecipeImage_;}

    public String getAuthor() {return this.mAuthor_;}

    public String getDateCreated() {return this.mDateCreated_;}

    public String getIngredients() {return this.mIngredients_;}

    public String getDescription() {return this.mDescription_;}

    public String getUserId() {return this.mUserId_;}


    public RecipeItem(String id, String recipeName,  String recipeImage, String author, String dateCreated, String ingredients, String description, String userId)
    {
        this.mId_ = id;
        this.mRecipeName_ = recipeName;
        this.mRecipeImage_ = recipeImage;
        this.mAuthor_ = author;
        this.mDateCreated_ = dateCreated;
        this.mIngredients_ = ingredients;
        this.mDescription_ = description;
        this.mUserId_ = userId;
    }
}
