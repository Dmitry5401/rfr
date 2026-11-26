package io.student.rangiffler.ui.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PhotoCard {
  private final Page page;
  private final Locator card;
  private final String description;

  public PhotoCard(Page page, Locator card, String description) {
    this.page = page;
    this.card = card;
    this.description = description;
  }

  @Step("Нажать like у фото: {this.description}")
  public PhotoCard clickLike() {
    card.getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName("like").setExact(true)).click();
    return this;
  }

  @Step("Открыть Photo actions у фото: {this.description}")
  public PhotoCard clickPhotoActions() {
    card.getByRole(AriaRole.BUTTON,
        new Locator.GetByRoleOptions().setName("Photo actions").setExact(true)).click();
    return this;
  }

  @Step("Выбрать Edit в меню фото: {this.description}")
  public PhotoCard clickEdit() {
    page.getByRole(AriaRole.MENUITEM,
        new Page.GetByRoleOptions().setName("Edit").setExact(true)).click();
    return this;
  }

  @Step("Выбрать Delete в меню фото: {this.description}")
  public PhotoCard clickDelete() {
    page.getByRole(AriaRole.MENUITEM,
        new Page.GetByRoleOptions().setName("Delete").setExact(true)).click();
    return this;
  }

  @Step("Проверить текст лайков у фото {this.description}: {text}")
  public PhotoCard checkThatLikesText(String text) {
    assertThat(card.getByText(text, new Locator.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }
}
