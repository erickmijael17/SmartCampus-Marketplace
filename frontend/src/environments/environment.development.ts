export const environment = {
  production: false,
  gatewayCandidates: [
    { label: 'DEV'  as const, url: 'http://localhost:18080' },
    { label: 'PROD' as const, url: 'http://localhost:28082' },
  ]
};
