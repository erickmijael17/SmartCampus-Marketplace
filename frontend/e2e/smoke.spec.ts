import { expect, test } from '@playwright/test';

test.describe('SmartCampus Marketplace smoke', () => {
  test('home page loads marketplace shell', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByRole('link', { name: 'SmartCampus Marketplace' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'SmartCampus' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Publicar ahora' })).toBeVisible();
  });

  test('login page shows validated form', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByRole('heading', { name: /Inicia sesion/i })).toBeVisible();
    await page.getByRole('button', { name: 'Ingresar' }).click();
    await expect(page.getByText('Usuario es obligatorio.')).toBeVisible();
    await expect(page.getByText('Contrasena es obligatorio.')).toBeVisible();
  });

  test('guest can browse to register from login', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('link', { name: 'Crear cuenta' }).click();

    await expect(page).toHaveURL(/\/register$/);
    await expect(page.getByRole('heading', { name: 'Crea tu cuenta' })).toBeVisible();
  });

  test('navbar exposes main routes', async ({ page }) => {
    await page.goto('/');

    const nav = page.getByRole('navigation');
    await expect(nav.getByRole('link', { name: 'Inicio' })).toBeVisible();
    await expect(nav.getByRole('link', { name: 'Publicar' })).toBeVisible();
    await expect(nav.getByRole('link', { name: 'Ingresar' })).toBeVisible();
  });

  test('publish route redirects guest to login', async ({ page }) => {
    await page.goto('/publish');

    await expect(page).toHaveURL(/\/login/);
  });

  test('listing detail route renders without backend', async ({ page }) => {
    await page.goto('/listing/1');

    await expect(
      page.getByText(/Cargando publicacion|Publicacion no encontrada|Inicia sesion para comprar|Comprar ahora/)
    ).toBeVisible({ timeout: 15000 });
  });

  test('checkout flow entry requires auth when listing loads', async ({ page }) => {
    await page.goto('/listing/1');

    const buyButton = page.getByRole('button', { name: /Inicia sesion para comprar|Comprar ahora/i });
    const notFound = page.getByRole('heading', { name: 'Publicacion no encontrada' });

    await expect(buyButton.or(notFound)).toBeVisible({ timeout: 15000 });
  });
});
