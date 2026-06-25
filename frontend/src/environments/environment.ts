export const environment = {
  production: true,
  gatewayCandidates: [
    { label: 'PROD' as const, url: 'http://localhost:28082' },
    { label: 'DEV'  as const, url: 'http://localhost:18080' },
  ]
};
